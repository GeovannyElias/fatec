package br.edu.fatecpg.rest_countries_pais.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pais {

    @JsonProperty("name")
    private Nome nome;

    @JsonProperty("capital")
    private String[] capital;

    @JsonProperty("region")
    private String regiao;

    @JsonProperty("subregion")
    private String subregiao;

    @JsonProperty("population")
    private long populacao;

    @JsonProperty("area")
    private double area;

    @JsonProperty("flags")
    private Map<String, String> flags;

    public Pais() {}

    public Nome getNome() {
        return nome;
    }

    public void setNome(Nome nome) {
        this.nome = nome;
    }

    public String getCapital() {
        if (capital != null && capital.length > 0) {
            return capital[0];
        }
        return "-";
    }

    public void setCapital(String[] capital) {
        this.capital = capital;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public String getSubregiao() {
        return subregiao;
    }

    public void setSubregiao(String subregiao) {
        this.subregiao = subregiao;
    }

    public long getPopulacao() {
        return populacao;
    }

    public void setPopulacao(long populacao) {
        this.populacao = populacao;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public void setFlags(Map<String, String> flags) {
        this.flags = flags;
    }

    public String getUrlBandeira() {
        return flags != null ? flags.get("png") : null;
    }

    @Override
    public String toString() {
        return "Pais{" +
                "nome=" + nome +
                ", capital='" + getCapital() + '\'' +
                ", regiao='" + regiao + '\'' +
                ", subregiao='" + subregiao + '\'' +
                ", populacao=" + populacao +
                ", area=" + area +
                '}';
    }
}