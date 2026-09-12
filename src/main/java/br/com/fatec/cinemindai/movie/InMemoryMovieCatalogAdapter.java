package br.com.fatec.cinemindai.movie;

import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub em memória, usado quando {@code movie.catalog=memory} (padrão). A integração real
 * com a OMDb fica em {@link br.com.fatec.cinemindai.movie.omdb.OmdbMovieCatalogAdapter}
 * ({@code movie.catalog=omdb}). Este stub continua existindo para testes e para rodar o agente
 * sem chave de API.
 */
@Component
@ConditionalOnProperty(name = "movie.catalog", havingValue = "memory", matchIfMissing = true)
public class InMemoryMovieCatalogAdapter implements MovieCatalogPort {

    private final List<MovieDetails> catalog = List.of(
            new MovieDetails("interestelar", "Interestelar", 2014, List.of("ficção científica", "drama"),
                    "Um grupo de astronautas viaja por um buraco de minhoca em busca de um novo lar para a humanidade.",
                    "Christopher Nolan", List.of("Matthew McConaughey", "Anne Hathaway"), 8.6),
            new MovieDetails("a-origem", "A Origem", 2010, List.of("ficção científica", "suspense"),
                    "Um ladrão que invade sonhos recebe a missão de plantar uma ideia na mente de um executivo.",
                    "Christopher Nolan", List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt"), 8.8),
            new MovieDetails("matrix", "Matrix", 1999, List.of("ficção científica", "ação"),
                    "Um hacker descobre que a realidade é uma simulação controlada por máquinas.",
                    "Lana Wachowski, Lilly Wachowski", List.of("Keanu Reeves", "Carrie-Anne Moss"), 8.7),
            new MovieDetails("clube-da-luta", "Clube da Luta", 1999, List.of("drama"),
                    "Um homem insone cria um clube de luta clandestino que sai do controle.",
                    "David Fincher", List.of("Brad Pitt", "Edward Norton"), 8.8),
            new MovieDetails("cidade-de-deus", "Cidade de Deus", 2002, List.of("drama", "crime"),
                    "A trajetória de jovens ligados ao crime organizado em uma favela do Rio de Janeiro.",
                    "Fernando Meirelles, Kátia Lund", List.of("Alexandre Rodrigues", "Leandro Firmino"), 8.6),
            new MovieDetails("parasita", "Parasita", 2019, List.of("drama", "suspense"),
                    "Uma família pobre se infiltra na vida de uma família rica com consequências inesperadas.",
                    "Bong Joon-ho", List.of("Song Kang-ho", "Lee Sun-kyun"), 8.5),
            new MovieDetails("o-poderoso-chefao", "O Poderoso Chefão", 1972, List.of("drama", "crime"),
                    "A saga da família Corleone, uma das mais poderosas famílias da máfia italoamericana.",
                    "Francis Ford Coppola", List.of("Marlon Brando", "Al Pacino"), 9.2),
            new MovieDetails("de-volta-para-o-futuro", "De Volta para o Futuro", 1985, List.of("ficção científica", "aventura", "comédia"),
                    "Um adolescente viaja acidentalmente 30 anos no passado em um carro modificado por seu amigo cientista.",
                    "Robert Zemeckis", List.of("Michael J. Fox", "Christopher Lloyd"), 8.5),
            new MovieDetails("coringa", "Coringa", 2019, List.of("drama", "crime"),
                    "A origem de um comediante fracassado que se transforma em um ícone do caos em Gotham.",
                    "Todd Phillips", List.of("Joaquin Phoenix", "Robert De Niro"), 8.4),
            new MovieDetails("wall-e", "WALL-E", 2008, List.of("animação", "ficção científica", "família"),
                    "Um robô solitário encarregado de limpar a Terra abandonada encontra um propósito inesperado.",
                    "Andrew Stanton", List.of("Ben Burtt", "Elissa Knight"), 8.4));

    @Override
    public List<MovieSummary> searchByTitle(String title) {
        String needle = normalize(title);
        return catalog.stream()
                .filter(m -> normalize(m.title()).contains(needle))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<MovieSummary> recommendByGenre(String genre) {
        String needle = normalize(genre);
        return catalog.stream()
                .filter(m -> m.genres().stream().anyMatch(g -> normalize(g).contains(needle)))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<MovieSummary> recommendSimilarTo(String title) {
        String needle = normalize(title);
        return catalog.stream()
                .filter(m -> normalize(m.title()).contains(needle))
                .findFirst()
                .map(reference -> catalog.stream()
                        .filter(m -> !m.id().equals(reference.id()))
                        .filter(m -> m.genres().stream().anyMatch(reference.genres()::contains))
                        .map(this::toSummary)
                        .toList())
                .orElseGet(List::of);
    }

    @Override
    public MovieDetails getDetails(String titleOrId) {
        String needle = normalize(titleOrId);
        return catalog.stream()
                .filter(m -> normalize(m.id()).equals(needle) || normalize(m.title()).contains(needle))
                .findFirst()
                .orElse(null);
    }

    private MovieSummary toSummary(MovieDetails details) {
        return new MovieSummary(details.id(), details.title(), details.year(), details.genres(), details.synopsis());
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
