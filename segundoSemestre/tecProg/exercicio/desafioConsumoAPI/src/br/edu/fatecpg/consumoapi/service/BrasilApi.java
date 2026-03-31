package br.edu.fatecpg.consumoapi.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BrasilApi {

    private static final String BASE_URL = "https://brasilapi.com.br/api/cnpj/v1/";

    /**
     * Consulta os dados de uma empresa na BrasilAPI pelo CNPJ (apenas dígitos).
     *
     * @param cnpj CNPJ já normalizado (somente números).
     * @return Corpo JSON da resposta.
     * @throws IOException              em falha de rede.
     * @throws InterruptedException     se a thread for interrompida.
     * @throws IllegalArgumentException se a API retornar status diferente de 200.
     */
    public static String buscaEmpresa(String cnpj)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + cnpj))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Valida o código HTTP antes de retornar o corpo
        if (response.statusCode() != 200) {
            throw new IllegalArgumentException(
                    "CNPJ não encontrado ou inválido. HTTP " + response.statusCode());
        }

        return response.body();
    }
}
