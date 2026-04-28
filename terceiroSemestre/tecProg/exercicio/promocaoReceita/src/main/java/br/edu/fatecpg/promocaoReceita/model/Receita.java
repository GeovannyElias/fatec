package br.edu.fatecpg.promocaoReceita.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_receitas")
public class Receita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String nome;
    private String categoria;
    private String preco;
    private Boolean promocao;

    public Receita() {
    }

    public Receita(String nome, String categoria, String preco, Boolean promocao) {
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
        this.promocao = promocao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPreco() {
        return preco;
    }

    public void setPreco(String preco) {
        this.preco = preco;
    }

    public Boolean getPromocao() {
        return promocao;
    }

    public void setPromocao(Boolean promocao) {
        this.promocao = promocao;
    }

    @Override
    public String toString() {
        return "Receita{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", categoria='" + categoria + '\'' +
                ", preco='" + preco + '\'' +
                ", promocao=" + promocao +
                '}';
    }
}
