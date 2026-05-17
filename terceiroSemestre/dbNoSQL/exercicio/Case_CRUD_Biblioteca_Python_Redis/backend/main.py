import asyncio
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from controllers.auth_controller import criar_admin_padrao
from controllers.auth_controller import router as auth_router
from controllers.livro_controller import processar_emprestimos_vencidos
from controllers.livro_controller import router as livro_router
from controllers.usuario_controller import router as usuario_router


async def verificar_emprestimos_vencidos():
    while True:
        try:
            processar_emprestimos_vencidos()
        except Exception:
            pass

        await asyncio.sleep(5)


@asynccontextmanager
async def lifespan(app: FastAPI):
    criar_admin_padrao()
    tarefa_emprestimos = asyncio.create_task(verificar_emprestimos_vencidos())

    try:
        yield
    finally:
        tarefa_emprestimos.cancel()


app = FastAPI(title="Biblioteca Municipal Online", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://127.0.0.1:5173",
        "http://localhost:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(livro_router)
app.include_router(usuario_router)


@app.get("/")
def raiz():
    return {
        "msg": "API Biblioteca em execucao",
        "docs": "Acesse http://127.0.0.1:8000/docs para ver a documentacao",
    }
