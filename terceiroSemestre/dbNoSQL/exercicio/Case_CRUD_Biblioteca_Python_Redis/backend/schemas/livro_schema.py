from pydantic import BaseModel, Field

class LivroCreate(BaseModel):
    titulo: str
    autor: str
    categoria: str
    ano: int
    quantidade: int = Field(gt=0)
    imagem: str = ""


class LivroUpdate(BaseModel):
    titulo: str
    autor: str
    categoria: str
    ano: int
    quantidade: int = Field(ge=0)
    imagem: str = ""
