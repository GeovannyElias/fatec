# Biblioteca Municipal Online

Sistema de Biblioteca Digital desenvolvido com Python, FastAPI, Redis, React e Tailwind CSS.

O sistema permite que um administrador gerencie o acervo da Biblioteca Municipal Online, cadastrando, atualizando e removendo livros. Os livros possuem titulo, autor, categoria, ano de publicacao, quantidade disponivel e status.

Usuarios comuns podem criar uma conta, consultar todos os livros disponiveis, pegar livros emprestados, favoritar livros e entrar em uma lista de espera quando um livro estiver sem estoque. Cada usuario pode ter no maximo 3 livros emprestados ao mesmo tempo.

Cada emprestimo dura 10 minutos. Se o usuario nao devolver o livro dentro desse prazo, o sistema devolve automaticamente o livro ao estoque e emitirá uma notificação. Quando um livro volta a ficar disponivel, os usuarios que estavam na lista de espera recebem uma notificacao dentro da propria aplicacao.

## Requisitos

- Python instalado
- Node.js instalado
- Docker instalado
- Redis rodando via Docker

## Como Rodar o Redis com Docker

Na raiz do projeto, execute:

```bash
docker run --name redis-biblioteca -p 6379:6379 -d redis
```

Para verificar se o container esta rodando:

```bash
docker ps
```

Se o container ja existir e estiver parado:

```bash
docker start redis-biblioteca
```

## Como Rodar o Backend

Entre na pasta do backend:

```bash
cd backend
```

Crie o arquivo `.env` com:

```env
REDIS_HOST=localhost
REDIS_PORT=6379
```

Instale as dependencias necessárias, caso ainda nao tenha instalado:

```bash
pip install fastapi uvicorn redis python-dotenv python-multipart
```

Rode a API:

```bash
python -m uvicorn main:app --reload
```

Backend:

```txt
http://127.0.0.1:8000
```

Documentacao da API:

```txt
http://127.0.0.1:8000/docs
```

## Como Rodar o Frontend

Em outro terminal, entre na pasta do frontend:

```bash
cd frontend
```

Instale as dependencias:

```bash
npm i
```

Rode o frontend:

```bash
npm run server
```

Frontend:

```txt
http://127.0.0.1:5173
```

## Login Admin

O usuario admin e criado automaticamente quando o backend inicia, caso ainda nao exista no Redis.

```txt
username: admin
senha: admin
```

Atalho:

```txt
admin/admin
```

## Observacoes

- O backend precisa estar rodando para o frontend consumir a API.
- O Redis precisa estar rodando antes de iniciar o backend.
- A documentacao dos endpoints fica disponivel em `/docs`.
- As imagens dos livros ficam em `frontend/public/livros` e sao servidas pelo frontend em `/livros/nome-da-imagem`.
