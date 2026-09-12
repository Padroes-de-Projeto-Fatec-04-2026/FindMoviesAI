package br.com.fatec.cinemindai.movie.omdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Resposta de {@code ?s=titulo}. A OMDb sinaliza erro com HTTP 200 e {@code "Response":"False"}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OmdbSearchResponse(
        @JsonProperty("Search") List<OmdbSearchItem> search,
        @JsonProperty("Response") String response,
        @JsonProperty("Error") String error) {

    public OmdbSearchResponse {
        search = search == null ? List.of() : List.copyOf(search);
    }

    public boolean ok() {
        return "True".equalsIgnoreCase(response);
    }
}
