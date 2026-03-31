package br.edu.fatecpg.rest_countries_pais.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Nome {

    @JsonProperty("common")
    private String comum;

    @JsonProperty("official")
    private String oficial;

    public Nome() {}

    public Nome(String comum, String oficial) {
        this.comum = comum;
        this.oficial = oficial;
    }

    public String getComum() {
        return comum;
    }

    public void setComum(String comum) {
        this.comum = comum;
    }

    public String getOficial() {
        return oficial;
    }

    public void setOficial(String oficial) {
        this.oficial = oficial;
    }
}