package br.edu.fatecpg.open_meteo_clima.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClimaDados {

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("current")
    private ClimaAtual ClimaAtual;

    private String erro;


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public ClimaAtual getClimaAtual() {
        return ClimaAtual;
    }

    public void setClimaAtual(ClimaAtual climaAtual) {
        ClimaAtual = climaAtual;
    }

    public void setErro(String mensagem) {
        this.erro = mensagem;
    }

    public String getErro() {
        return erro;
    }
}
