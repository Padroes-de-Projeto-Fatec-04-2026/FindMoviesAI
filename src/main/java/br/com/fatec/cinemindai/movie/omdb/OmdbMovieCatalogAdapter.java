package br.com.fatec.cinemindai.movie.omdb;

import br.com.fatec.cinemindai.movie.MovieCatalogPort;
import br.com.fatec.cinemindai.movie.MovieDetails;
import br.com.fatec.cinemindai.movie.MovieSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação real do {@link MovieCatalogPort} sobre a API pública da OMDb
 * (https://www.omdbapi.com/). Ativada com {@code movie.catalog=omdb}.
 *
 * <p>Como a OMDb só busca por título, "recomendar por gênero" e "filmes parecidos" são
 * montados a partir de buscas por termos ({@link OmdbGenre#searchTerms()}) filtradas pelo
 * campo {@code Genre} de cada candidato. Cada candidato custa uma chamada de detalhes, então
 * {@link OmdbProperties#maxCandidates()} limita o gasto da cota diária (1.000 req no plano
 * gratuito).
 */
public class OmdbMovieCatalogAdapter implements MovieCatalogPort {

    private final OmdbClient client;
    private final OmdbProperties properties;

    public OmdbMovieCatalogAdapter(OmdbClient client, OmdbProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public List<MovieSummary> searchByTitle(String title) {
        if (title == null || title.isBlank()) {
            return List.of();
        }
        return client.search(title).stream()
                .limit(properties.maxResults())
                .map(item -> client.findById(item.imdbId()))
                .flatMap(Optional::stream)
                .map(OmdbMovie::toSummary)
                .toList();
    }

    @Override
    public List<MovieSummary> recommendByGenre(String genre) {
        return OmdbGenre.resolve(genre)
                .map(resolved -> findByGenre(resolved, null))
                .orElseGet(List::of);
    }

    @Override
    public List<MovieSummary> recommendSimilarTo(String title) {
        Optional<OmdbMovie> reference = findMovie(title);
        if (reference.isEmpty()) {
            return List.of();
        }
        // Usa o gênero principal do filme de referência, excluindo o próprio filme dos resultados.
        OmdbMovie movie = reference.get();
        return movie.genres().stream()
                .map(OmdbGenre::resolve)
                .flatMap(Optional::stream)
                .findFirst()
                .map(genre -> findByGenre(genre, movie.imdbId()))
                .orElseGet(List::of);
    }

    @Override
    public MovieDetails getDetails(String titleOrId) {
        return findMovie(titleOrId).map(OmdbMovie::toDetails).orElse(null);
    }

    private Optional<OmdbMovie> findMovie(String titleOrId) {
        if (titleOrId == null || titleOrId.isBlank()) {
            return Optional.empty();
        }
        String value = titleOrId.trim();
        return value.startsWith("tt") ? client.findById(value) : client.findByTitle(value);
    }

    private List<MovieSummary> findByGenre(OmdbGenre genre, String excludeImdbId) {
        // Coleta candidatos (sem duplicatas, preservando a ordem de relevância da OMDb).
        Map<String, OmdbSearchItem> candidates = new LinkedHashMap<>();
        for (String term : genre.searchTerms()) {
            if (candidates.size() >= properties.maxCandidates()) {
                break;
            }
            for (OmdbSearchItem item : client.search(term)) {
                if (candidates.size() >= properties.maxCandidates()) {
                    break;
                }
                if (!item.imdbId().equals(excludeImdbId)) {
                    candidates.putIfAbsent(item.imdbId(), item);
                }
            }
        }

        // Busca os detalhes e mantém só os que realmente pertencem ao gênero.
        List<MovieSummary> results = new ArrayList<>();
        for (OmdbSearchItem candidate : candidates.values()) {
            if (results.size() >= properties.maxResults()) {
                break;
            }
            client.findById(candidate.imdbId())
                    .filter(movie -> genre.matches(movie.genres()))
                    .map(OmdbMovie::toSummary)
                    .ifPresent(results::add);
        }
        return results;
    }
}
