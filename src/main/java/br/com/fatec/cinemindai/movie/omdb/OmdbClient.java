package br.com.fatec.cinemindai.movie.omdb;

import java.util.List;
import java.util.Optional;
import org.springframework.web.client.RestClient;

/**
 * Wrapper fino sobre os dois endpoints da OMDb: busca por título ({@code s=}) e
 * detalhes por IMDb ID ({@code i=}) ou por título exato ({@code t=}).
 */
public class OmdbClient {

    private final RestClient restClient;
    private final String apiKey;

    public OmdbClient(RestClient.Builder builder, OmdbProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
        this.apiKey = properties.apiKey();
    }

    public List<OmdbSearchItem> search(String term) {
        OmdbSearchResponse response = restClient.get()
                .uri(uri -> uri.queryParam("apikey", apiKey)
                        .queryParam("s", term)
                        .queryParam("type", "movie")
                        .build())
                .retrieve()
                .body(OmdbSearchResponse.class);
        return response == null || !response.ok() ? List.of() : response.search();
    }

    public Optional<OmdbMovie> findById(String imdbId) {
        return fetch("i", imdbId);
    }

    public Optional<OmdbMovie> findByTitle(String title) {
        return fetch("t", title);
    }

    private Optional<OmdbMovie> fetch(String param, String value) {
        OmdbMovie movie = restClient.get()
                .uri(uri -> uri.queryParam("apikey", apiKey)
                        .queryParam(param, value)
                        .queryParam("type", "movie")
                        .queryParam("plot", "short")
                        .build())
                .retrieve()
                .body(OmdbMovie.class);
        return movie == null || !movie.ok() ? Optional.empty() : Optional.of(movie);
    }
}
