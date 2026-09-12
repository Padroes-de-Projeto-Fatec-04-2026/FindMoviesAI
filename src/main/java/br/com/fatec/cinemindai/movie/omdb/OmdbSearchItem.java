package br.com.fatec.cinemindai.movie.omdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Item da lista de busca: não traz gênero nem sinopse, por isso exige uma chamada de detalhes. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OmdbSearchItem(
        @JsonProperty("Title") String title,
        @JsonProperty("Year") String year,
        @JsonProperty("imdbID") String imdbId) {
}
