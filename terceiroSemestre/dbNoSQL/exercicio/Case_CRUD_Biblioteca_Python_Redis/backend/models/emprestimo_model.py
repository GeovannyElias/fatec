from pydantic import BaseModel


class Emprestimo(BaseModel):
    username: str
    livro_id: int
    devolucao_em: int
