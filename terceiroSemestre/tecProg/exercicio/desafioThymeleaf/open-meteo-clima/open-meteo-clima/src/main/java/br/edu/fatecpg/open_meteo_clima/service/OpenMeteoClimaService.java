package br.edu.fatecpg.open_meteo_clima.service;

import br.edu.fatecpg.open_meteo_clima.model.ClimaDados;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenMeteoClimaService {

    @Value("${open-meteo.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public ClimaDados buscarClima(double latitude, double longitude) {

        String url = apiUrl
                .replace("{lat}", String.valueOf(latitude))
                .replace("{lon}", String.valueOf(longitude));

        try {
            ClimaDados dados = restTemplate.getForObject(url, ClimaDados.class);

            if (dados == null || dados.getClimaAtual() == null) {
                return erro("Não foi possível obter dados da API.");
            }

            return dados;

        } catch (HttpClientErrorException e) {
            System.out.println("Erro HTTP: " + e.getStatusCode());
            return erro("Erro na requisição (verifique parâmetros).");

        } catch (Exception e) {
            System.out.println("Erro geral: " + e.getMessage());
            return erro("Serviço indisponível no momento.");
        }
    }

    private ClimaDados erro(String mensagem) {
        ClimaDados erro = new ClimaDados();

        erro.setErro(mensagem);
        return erro;
    }

    public String descricaoClima(int codigo) {
        if (codigo == 0) {
            return "Céu Limpo";
        } else if (codigo >= 1 && codigo <= 3) {
            return "Parcialmente Nublado";
        } else if (codigo >= 45 && codigo <= 48) {
            return "Neblina";
        } else if (codigo >= 61 && codigo <= 67) {
            return "Chuva";
        } else if (codigo >= 71 && codigo <= 77) {
            return "Neve";
        } else if (codigo >= 80 && codigo <= 82) {
            return "Pancadas de Chuva";
        } else {
            return "Condição Desconhecida";
        }
    }
}