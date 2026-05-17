import time
import unicodedata
from pathlib import Path

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile
from pydantic import ValidationError
from redis.exceptions import RedisError

from controllers.auth_controller import exigir_admin, exigir_user, tratar_erro_redis
from database.redis_db import r
from enums.status_enum import Status
from models.livro_model import Livro
from schemas.livro_schema import LivroCreate, LivroUpdate

router = APIRouter(tags=["Livros"])

PRAZO_EMPRESTIMO_SEGUNDOS = 600
MAX_EMPRESTIMOS_USUARIO = 3
EXTENSOES_IMAGEM_PERMITIDAS = {".png", ".jpg", ".jpeg", ".webp"}
TIPOS_IMAGEM_PERMITIDOS = {"image/png", "image/jpeg", "image/webp"}
MAX_IMAGEM_BYTES = 5 * 1024 * 1024
PASTA_IMAGENS_LIVROS = (
    Path(__file__).resolve().parents[2] / "frontend" / "public" / "livros"
)


def chave_livro(livro_id: int) -> str:
    return f"livro:{livro_id}"


def chave_emprestimo(username: str, livro_id: int) -> str:
    return f"emprestimo:{username}:{livro_id}"


def chave_emprestimos_usuario(username: str) -> str:
    return f"usuario:{username}:emprestimos"


def chave_favoritos_usuario(username: str) -> str:
    return f"usuario:{username}:favoritos"


def chave_espera_usuario(username: str) -> str:
    return f"usuario:{username}:espera"


def chave_espera_livro(livro_id: int) -> str:
    return f"livro:{livro_id}:espera"


def chave_notificacoes_usuario(username: str) -> str:
    return f"usuario:{username}:notificacoes"


def gerar_nome_imagem(titulo: str) -> str:
    texto = unicodedata.normalize("NFKD", titulo)
    texto = "".join(char for char in texto if not unicodedata.combining(char))
    texto = texto.lower()
    texto = "".join(char if char.isalnum() else "_" for char in texto)
    partes = [parte for parte in texto.split("_") if parte]
    return "_".join(partes) or "livro"


def garantir_pasta_imagens():
    PASTA_IMAGENS_LIVROS.mkdir(parents=True, exist_ok=True)


def imagens_em_uso(livro_id_ignorado: int | None = None) -> set[str]:
    nomes = set()

    for chave in r.scan_iter("livro:*"):
        if ":espera" in chave:
            continue

        livro = r.hgetall(chave)
        if not livro.get("imagem"):
            continue

        if livro_id_ignorado is not None and int(livro.get("id", 0)) == livro_id_ignorado:
            continue

        nomes.add(livro["imagem"])

    return nomes


def gerar_nome_disponivel(titulo: str, extensao: str, livro_id_ignorado: int | None = None) -> str:
    base = gerar_nome_imagem(titulo)
    nomes_usados = imagens_em_uso(livro_id_ignorado)
    contador = 1

    while True:
        sufixo = "" if contador == 1 else f"_{contador}"
        nome = f"{base}{sufixo}{extensao}"
        if nome not in nomes_usados and not (PASTA_IMAGENS_LIVROS / nome).exists():
            return nome
        contador += 1


def deletar_imagem_antiga(nome_imagem: str | None):
    if not nome_imagem:
        return

    caminho = PASTA_IMAGENS_LIVROS / Path(nome_imagem).name
    if caminho.exists() and caminho.is_file():
        caminho.unlink()


async def salvar_nova_imagem(imagem: UploadFile | None, titulo: str, livro_id_ignorado: int | None = None) -> str:
    if not imagem or not imagem.filename:
        return ""

    garantir_pasta_imagens()
    extensao = Path(imagem.filename).suffix.lower()

    if extensao not in EXTENSOES_IMAGEM_PERMITIDAS:
        raise HTTPException(status_code=400, detail="Formato de imagem invalido")

    if imagem.content_type not in TIPOS_IMAGEM_PERMITIDOS:
        raise HTTPException(status_code=400, detail="Tipo de arquivo invalido")

    conteudo = await imagem.read()
    if not conteudo:
        raise HTTPException(status_code=400, detail="Imagem vazia")

    if len(conteudo) > MAX_IMAGEM_BYTES:
        raise HTTPException(status_code=400, detail="Imagem maior que 5 MB")

    nome = gerar_nome_disponivel(titulo, extensao, livro_id_ignorado)
    (PASTA_IMAGENS_LIVROS / nome).write_bytes(conteudo)
    return nome


def atualizar_imagem_ao_editar(
    titulo: str,
    imagem_antiga: str | None,
    nova_imagem: str,
    livro_id: int,
) -> str:
    if nova_imagem:
        deletar_imagem_antiga(imagem_antiga)
        return nova_imagem

    if not imagem_antiga:
        return ""

    garantir_pasta_imagens()
    extensao = Path(imagem_antiga).suffix.lower()
    nome_base_atualizado = gerar_nome_imagem(titulo)
    nome_antigo_sem_extensao = Path(imagem_antiga).stem

    if nome_antigo_sem_extensao == nome_base_atualizado or nome_antigo_sem_extensao.startswith(
        f"{nome_base_atualizado}_"
    ):
        return imagem_antiga

    nome_atualizado = gerar_nome_disponivel(titulo, extensao, livro_id)

    if nome_atualizado == imagem_antiga:
        return imagem_antiga

    caminho_antigo = PASTA_IMAGENS_LIVROS / Path(imagem_antiga).name
    caminho_novo = PASTA_IMAGENS_LIVROS / nome_atualizado

    if caminho_antigo.exists() and caminho_antigo.is_file():
        caminho_antigo.rename(caminho_novo)
        return nome_atualizado

    return ""


def validar_livro(dados_livro: dict):
    try:
        dados_livro.setdefault("imagem", "")
        return Livro(**dados_livro)
    except ValidationError as exc:
        raise HTTPException(
            status_code=500,
            detail="Dados do livro armazenados em formato invalido",
        ) from exc


def gerar_id():
    try:
        return r.incr("livro_id")
    except RedisError:
        tratar_erro_redis()


def atualizar_status_livro(livro_id: int):
    livro = r.hgetall(chave_livro(livro_id))

    if not livro:
        return

    quantidade = int(livro.get("quantidade", 0))
    status = Status.Disponivel.value if quantidade > 0 else Status.Emprestado.value
    r.hset(chave_livro(livro_id), mapping={"status": status})


def notificar_usuarios_em_espera(livro_id: int):
    usuarios = r.smembers(chave_espera_livro(livro_id))
    livro = r.hgetall(chave_livro(livro_id))
    titulo = livro.get("titulo", f"Livro #{livro_id}")

    for username in usuarios:
        r.rpush(chave_notificacoes_usuario(username), f'"{titulo}" voltou ao estoque')
        r.srem(chave_espera_usuario(username), livro_id)

    r.delete(chave_espera_livro(livro_id))


def devolver_emprestimo(username: str, livro_id: int, notificar_atraso: bool = False):
    chave = chave_emprestimo(username, livro_id)

    if not r.exists(chave):
        raise HTTPException(status_code=404, detail="Emprestimo nao encontrado")

    livro = r.hgetall(chave_livro(livro_id))
    titulo = livro.get("titulo", f"Livro #{livro_id}")

    r.delete(chave)
    r.srem(chave_emprestimos_usuario(username), livro_id)
    r.zrem("emprestimos_vencimento", f"{username}:{livro_id}")
    r.hincrby(chave_livro(livro_id), "quantidade", 1)
    atualizar_status_livro(livro_id)

    if notificar_atraso:
        r.rpush(
            chave_notificacoes_usuario(username),
            f'Removemos "{titulo}" da sua conta. Tempo limite atingido',
        )

    notificar_usuarios_em_espera(livro_id)


def processar_emprestimos_vencidos():
    agora = int(time.time())

    try:
        vencidos = r.zrangebyscore("emprestimos_vencimento", 0, agora)

        for item in vencidos:
            username, livro_id_texto = item.split(":", 1)
            livro_id = int(livro_id_texto)

            if r.exists(chave_emprestimo(username, livro_id)):
                devolver_emprestimo(username, livro_id, notificar_atraso=True)
            else:
                r.zrem("emprestimos_vencimento", item)
    except RedisError:
        tratar_erro_redis()


def livro_tem_emprestimos_ativos(livro_id: int) -> bool:
    for chave in r.scan_iter(f"emprestimo:*:{livro_id}"):
        if r.exists(chave):
            return True

    return False


def emprestar_livro(username: str, livro_id: int):
    processar_emprestimos_vencidos()

    try:
        livro = r.hgetall(chave_livro(livro_id))

        if not livro:
            raise HTTPException(status_code=404, detail="Livro nao encontrado")

        if r.exists(chave_emprestimo(username, livro_id)):
            raise HTTPException(status_code=409, detail="Usuario ja esta com este livro")

        total_emprestimos = r.scard(chave_emprestimos_usuario(username))
        if total_emprestimos >= MAX_EMPRESTIMOS_USUARIO:
            raise HTTPException(
                status_code=400,
                detail="Usuario pode ter no maximo 3 livros emprestados",
            )

        quantidade = int(livro.get("quantidade", 0))
        if quantidade <= 0:
            raise HTTPException(
                status_code=409,
                detail="Livro sem estoque. Adicione na espera para ser notificado",
            )

        devolucao_em = int(time.time()) + PRAZO_EMPRESTIMO_SEGUNDOS
        r.hincrby(chave_livro(livro_id), "quantidade", -1)
        atualizar_status_livro(livro_id)
        r.hset(
            chave_emprestimo(username, livro_id),
            mapping={
                "username": username,
                "livro_id": livro_id,
                "devolucao_em": devolucao_em,
            },
        )
        r.sadd(chave_emprestimos_usuario(username), livro_id)
        r.zadd("emprestimos_vencimento", {f"{username}:{livro_id}": devolucao_em})
    except RedisError:
        tratar_erro_redis()

    return {
        "msg": "Livro emprestado com sucesso",
        "livro_id": livro_id,
        "devolucao_em": devolucao_em,
    }


def devolver_livro(username: str, livro_id: int):
    processar_emprestimos_vencidos()

    try:
        devolver_emprestimo(username, livro_id)
    except RedisError:
        tratar_erro_redis()

    return {"msg": "Livro devolvido com sucesso", "livro_id": livro_id}


@router.post("/livros", status_code=201, dependencies=[Depends(exigir_admin)])
async def criar_livro(
    titulo: str = Form(...),
    autor: str = Form(...),
    categoria: str = Form(...),
    ano: int = Form(...),
    quantidade: int = Form(...),
    imagem: UploadFile | None = File(None),
):
    livro = LivroCreate(
        titulo=titulo,
        autor=autor,
        categoria=categoria,
        ano=ano,
        quantidade=quantidade,
    )
    novo_id = gerar_id()
    chave = chave_livro(novo_id)

    dados_livro = livro.model_dump()
    dados_livro["id"] = novo_id
    dados_livro["status"] = Status.Disponivel.value
    imagem_salva = ""

    try:
        imagem_salva = await salvar_nova_imagem(imagem, livro.titulo)
        dados_livro["imagem"] = imagem_salva
        r.hset(chave, mapping=dados_livro)
    except RedisError:
        deletar_imagem_antiga(imagem_salva)
        tratar_erro_redis()

    return {"msg": "Livro criado com sucesso", "id": novo_id}


@router.get("/livros", response_model=list[Livro])
def listar_livros():
    livros = []
    processar_emprestimos_vencidos()

    try:
        for chave in r.scan_iter("livro:*"):
            if ":espera" in chave:
                continue

            livros.append(validar_livro(r.hgetall(chave)))
    except RedisError:
        tratar_erro_redis()

    return livros


@router.get("/livros/{livro_id}", response_model=Livro)
def buscar_livro(livro_id: int):
    processar_emprestimos_vencidos()
    chave = chave_livro(livro_id)

    try:
        dados_livro = r.hgetall(chave)
    except RedisError:
        tratar_erro_redis()

    if not dados_livro:
        raise HTTPException(status_code=404, detail="Livro nao encontrado")

    return validar_livro(dados_livro)


@router.put("/livros/{livro_id}", dependencies=[Depends(exigir_admin)])
async def atualizar_livro(
    livro_id: int,
    titulo: str = Form(...),
    autor: str = Form(...),
    categoria: str = Form(...),
    ano: int = Form(...),
    quantidade: int = Form(...),
    imagem: UploadFile | None = File(None),
):
    chave = chave_livro(livro_id)
    livro = LivroUpdate(
        titulo=titulo,
        autor=autor,
        categoria=categoria,
        ano=ano,
        quantidade=quantidade,
    )
    dados_livro = livro.model_dump()
    dados_livro["id"] = livro_id
    dados_livro["status"] = (
        Status.Disponivel.value if dados_livro["quantidade"] > 0 else Status.Emprestado.value
    )

    nova_imagem = ""

    try:
        livro_atual = r.hgetall(chave)
        if not livro_atual:
            raise HTTPException(status_code=404, detail="Livro nao encontrado")

        nova_imagem = await salvar_nova_imagem(imagem, livro.titulo, livro_id)
        dados_livro["imagem"] = atualizar_imagem_ao_editar(
            livro.titulo,
            livro_atual.get("imagem", ""),
            nova_imagem,
            livro_id,
        )
        r.hset(chave, mapping=dados_livro)
    except RedisError:
        deletar_imagem_antiga(nova_imagem)
        tratar_erro_redis()

    return {"msg": "Livro atualizado com sucesso"}


@router.delete("/livros/{livro_id}", dependencies=[Depends(exigir_admin)])
def deletar_livro(livro_id: int):
    chave = chave_livro(livro_id)
    processar_emprestimos_vencidos()

    try:
        livro = r.hgetall(chave)
        if livro_tem_emprestimos_ativos(livro_id):
            raise HTTPException(
                status_code=409,
                detail="Livro possui emprestimos ativos e nao pode ser apagado",
            )

        removidos = r.delete(chave)
    except RedisError:
        tratar_erro_redis()

    if removidos == 0:
        raise HTTPException(status_code=404, detail="Livro nao encontrado")

    deletar_imagem_antiga(livro.get("imagem", ""))

    return {"msg": "Livro deletado com sucesso"}


@router.post("/livros/{livro_id}/emprestar")
def emprestar(livro_id: int, usuario: dict = Depends(exigir_user)):
    return emprestar_livro(usuario["username"], livro_id)


@router.post("/livros/{livro_id}/devolver")
def devolver(livro_id: int, usuario: dict = Depends(exigir_user)):
    return devolver_livro(usuario["username"], livro_id)
