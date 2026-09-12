package br.com.fatec.cinemindai.movie.omdb;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do cliente OMDb (prefixo {@code omdb.*} no application.properties).
 *
 * @param baseUrl       URL base da API (padrão: https://www.omdbapi.com/)
 * @param apiKey        chave obtida em https://www.omdbapi.com/apikey.aspx
 * @param maxResults    quantos filmes cada consulta devolve ao agente
 * @param maxCandidates quantos resultados de busca são inspecionados (1 chamada de detalhes
 *                      cada) ao montar recomendações por gênero — controla o gasto de cota
 */
@ConfigurationProperties(prefix = "omdb")
public record OmdbProperties(String baseUrl, String apiKey, int maxResults, int maxCandidates) {

    public OmdbProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank() ? "https://www.omdbapi.com/" : baseUrl;
        maxResults = maxResults <= 0 ? 5 : maxResults;
        maxCandidates = maxCandidates <= 0 ? 12 : maxCandidates;
    }
}
