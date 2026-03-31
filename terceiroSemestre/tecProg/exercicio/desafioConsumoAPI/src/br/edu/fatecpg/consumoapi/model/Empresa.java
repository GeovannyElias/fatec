package br.edu.fatecpg.consumoapi.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Empresa {

    private int id;

    @SerializedName("cnpj")
    private String cnpj;

    @SerializedName("razao_social")
    private String razaoSocial;

    @SerializedName("nome_fantasia")
    private String nomeFantasia;

    @SerializedName("logradouro")
    private String logradouro;

    /** O Gson popula esta lista automaticamente ao fazer fromJson(). */
    @SerializedName("qsa")
    private List<Socio> qsa = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /** Construtor sem argumentos — necessário para o DAO montar o objeto via ResultSet. */
    public Empresa() {}

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getId()            { return id; }
    public String getCnpj()       { return cnpj; }
    public String getRazaoSocial(){ return razaoSocial; }
    public String getNomeFantasia(){ return nomeFantasia; }
    public String getLogradouro() { return logradouro; }
    public List<Socio> getQsa()   { return qsa; }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setId(int id)                    { this.id = id; }
    public void setCnpj(String cnpj)             { this.cnpj = cnpj; }
    public void setRazaoSocial(String razaoSocial){ this.razaoSocial = razaoSocial; }
    public void setNomeFantasia(String nomeFantasia){ this.nomeFantasia = nomeFantasia; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public void setQsa(List<Socio> qsa)          { this.qsa = qsa; }

    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Empresa{" +
                "cnpj='"         + cnpj         + '\'' +
                ", razaoSocial='" + razaoSocial  + '\'' +
                ", nomeFantasia='" + nomeFantasia + '\'' +
                ", logradouro='"  + logradouro   + '\'' +
                ", qsa="          + qsa          +
                '}';
    }
}
