package br.com.fatec.cinemindai.movie.omdb;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A OMDb não tem busca por gênero — só por título. Este enum traduz o gênero pedido pelo
 * usuário (em português ou inglês) para o nome usado no campo {@code Genre} da OMDb e para
 * alguns termos de busca que costumam trazer filmes populares daquele gênero; os resultados
 * são depois filtrados pelo {@code Genre} real de cada filme.
 */
public enum OmdbGenre {

    ACTION("Action", List.of("acao", "action"), List.of("mission", "fury", "bourne")),
    ADVENTURE("Adventure", List.of("aventura", "adventure"), List.of("jones", "jurassic", "quest")),
    ANIMATION("Animation", List.of("animacao", "animation", "desenho"), List.of("toy story", "shrek", "kung fu panda")),
    BIOGRAPHY("Biography", List.of("biografia", "biography", "cinebiografia"), List.of("theory", "social network", "oppenheimer")),
    COMEDY("Comedy", List.of("comedia", "comedy"), List.of("hangover", "wedding", "crazy")),
    CRIME("Crime", List.of("crime", "policial", "gangster", "mafia"), List.of("heist", "godfather", "gangster")),
    DOCUMENTARY("Documentary", List.of("documentario", "documentary"), List.of("documentary", "planet")),
    DRAMA("Drama", List.of("drama"), List.of("story", "life", "beautiful")),
    FAMILY("Family", List.of("familia", "family", "infantil"), List.of("home alone", "paddington", "wonder")),
    FANTASY("Fantasy", List.of("fantasia", "fantasy"), List.of("dragon", "harry potter", "rings")),
    HISTORY("History", List.of("historia", "historico", "history", "historical"), List.of("kingdom", "gladiator", "king")),
    HORROR("Horror", List.of("terror", "horror"), List.of("horror", "dead", "conjuring")),
    MUSICAL("Musical", List.of("musical", "musica", "music"), List.of("musical", "la la land", "mamma mia")),
    MYSTERY("Mystery", List.of("misterio", "mystery"), List.of("mystery", "gone", "knives")),
    ROMANCE("Romance", List.of("romance", "romantico", "romantic"), List.of("love", "notebook", "before")),
    SCI_FI("Sci-Fi", List.of("ficcao cientifica", "ficcao", "sci-fi", "scifi", "science fiction"), List.of("space", "alien", "star")),
    SPORT("Sport", List.of("esporte", "esportes", "sport", "sports"), List.of("rocky", "creed", "field")),
    THRILLER("Thriller", List.of("suspense", "thriller"), List.of("silence", "seven", "prisoners")),
    WAR("War", List.of("guerra", "war"), List.of("war", "saving", "dunkirk")),
    WESTERN("Western", List.of("faroeste", "western", "velho oeste"), List.of("django", "unforgiven", "western"));

    private final String omdbName;
    private final List<String> aliases;
    private final List<String> searchTerms;

    OmdbGenre(String omdbName, List<String> aliases, List<String> searchTerms) {
        this.omdbName = omdbName;
        this.aliases = aliases;
        this.searchTerms = searchTerms;
    }

    public String omdbName() {
        return omdbName;
    }

    public List<String> searchTerms() {
        return searchTerms;
    }

    /** Verifica se o campo {@code Genre} de um filme (já separado por vírgula) inclui este gênero. */
    public boolean matches(List<String> movieGenres) {
        return movieGenres.stream().anyMatch(g -> g.equalsIgnoreCase(omdbName));
    }

    /** Resolve "Ficção Científica", "sci-fi", "Terror", "Horror"... para o gênero OMDb correspondente. */
    public static Optional<OmdbGenre> resolve(String genre) {
        String needle = normalize(genre);
        if (needle.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(g -> g.aliases.stream().anyMatch(alias -> alias.equals(needle) || needle.contains(alias)))
                .findFirst();
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
