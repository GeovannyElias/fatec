package br.edu.fatecpg.consumoapi.model;

import com.google.gson.annotations.SerializedName;

public class Socio {

    private int id;

    @SerializedName("nome_socio")
    private String nomeSocio;

    @SerializedName("cnpj_cpf_do_socio")
    private String cnpjCpfDoSocio;

    @SerializedName("qualificacao_socio")
    private String qualificacaoSocio;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /** Construtor sem argumentos — necessário para o DAO e para o Gson. */
    public Socio() {}

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getId()                  { return id; }
    public String getNomeSocio()        { return nomeSocio; }
    public String getCnpjCpfDoSocio()   { return cnpjCpfDoSocio; }
    public String getQualificacaoSocio(){ return qualificacaoSocio; }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setId(int id)                           { this.id = id; }
    public void setNomeSocio(String nomeSocio)          { this.nomeSocio = nomeSocio; }
    public void setCnpjCpfDoSocio(String cnpjCpfDoSocio){ this.cnpjCpfDoSocio = cnpjCpfDoSocio; }
    public void setQualificacaoSocio(String qualificacaoSocio){ this.qualificacaoSocio = qualificacaoSocio; }

    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Socio{" +
                "nomeSocio='"        + nomeSocio        + '\'' +
                ", cnpjCpfDoSocio='" + cnpjCpfDoSocio   + '\'' +
                ", qualificacao='"   + qualificacaoSocio + '\'' +
                '}';
    }
}
