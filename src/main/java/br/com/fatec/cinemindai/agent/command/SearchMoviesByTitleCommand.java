package br.com.fatec.cinemindai.agent.command;

import br.com.fatec.cinemindai.movie.MovieCatalogPort;
import br.com.fatec.cinemindai.movie.MovieSummary;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SearchMoviesByTitleCommand implements AgentCommand {

    private final MovieCatalogPort catalog;

    public SearchMoviesByTitleCommand(MovieCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public String name() {
        return "search_movies_by_title";
    }

    @Override
    public String description() {
        return "Busca filmes pelo título. Argumentos: {\"title\": string}.";
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String title = context.argument("title");
        List<MovieSummary> results = catalog.searchByTitle(title == null ? "" : title);
        String summary = results.isEmpty()
                ? "Nenhum filme encontrado para \"" + title + "\"."
                : "Encontrados " + results.size() + " filme(s) para \"" + title + "\".";
        return new CommandResult(!results.isEmpty(), summary, results);
    }
}
