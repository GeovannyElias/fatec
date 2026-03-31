package br.edu.fatecpg.open_meteo_clima.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClimaAtual {

    @JsonProperty("time")
    private String time;

    @JsonProperty("temperature_2m")
    private double temperature_2m;

    @JsonProperty("windspeed_10m")
    private double windspeed_10m;

    @JsonProperty("weathercode")
    private int weathercode;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getTemperature_2m() {
        return temperature_2m;
    }

    public void setTemperature_2m(double temperature_2m) {
        this.temperature_2m = temperature_2m;
    }

    public double getWindspeed_10m() {
        return windspeed_10m;
    }

    public void setWindspeed_10m(double windspeed_10m) {
        this.windspeed_10m = windspeed_10m;
    }

    public int getWeathercode() {
        return weathercode;
    }

    public void setWeathercode(int weathercode) {
        this.weathercode = weathercode;
    }
}
