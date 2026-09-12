package br.com.fatec.cinemindai.movie.omdb;

import br.com.fatec.cinemindai.movie.MovieDetails;
import br.com.fatec.cinemindai.movie.MovieSummary;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resposta de {@code ?i=imdbID} / {@code ?t=titulo}. Campos ausentes vêm como a string
 * {@code "N/A"}; o ano pode vir como intervalo ({@code "2014–2019"}) para séries.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OmdbMovie(
        @JsonProperty("imdbID") String imdbId,
        @JsonProperty("Title") String title,
        @JsonProperty("Year") String year,
        @JsonProperty("Genre") String genre,
        @JsonProperty("Plot") String plot,
        @JsonProperty("Director") String director,
        @JsonProperty("Actors") String actors,
        @JsonProperty("imdbRating") String imdbRating,
        @JsonProperty("Response") String response) {

    private static final String NOT_AVAILABLE = "N/A";
    private static final Pattern YEAR = Pattern.compile("\\d{4}");

    public boolean ok() {
        return "True".equalsIgnoreCase(response);
    }

    public List<String> genres() {
        return splitList(genre);
    }

    public MovieSummary toSummary() {
        return new MovieSummary(imdbId, title, parseYear(year), genres(), textOrEmpty(plot));
    }

    public MovieDetails toDetails() {
        return new MovieDetails(imdbId, title, parseYear(year), genres(), textOrEmpty(plot),
                textOrEmpty(director), splitList(actors), parseRating(imdbRating));
    }

    static int parseYear(String value) {
        Matcher matcher = YEAR.matcher(value == null ? "" : value);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

    static double parseRating(String value) {
        try {
            return isBlank(value) ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    static List<String> splitList(String value) {
        return isBlank(value)
                ? List.of()
                : Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static String textOrEmpty(String value) {
        return isBlank(value) ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank() || NOT_AVAILABLE.equalsIgnoreCase(value);
    }
}
