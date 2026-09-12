package br.com.fatec.cinemindai.agent.command;

import br.com.fatec.cinemindai.movie.MovieCatalogPort;
import br.com.fatec.cinemindai.movie.MovieSummary;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendSimilarMoviesCommand implements AgentCommand {

    private final MovieCatalogPort catalog;

    public RecommendSimilarMoviesCommand(MovieCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public String name() {
        return "recommend_similar_movies";
    }

    @Override
    public String description() {
        return "Recomenda filmes parecidos com um filme de referência. Argumentos: {\"title\": string, título original em inglês}.";
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String title = context.argument("title");
        List<MovieSummary> results = catalog.recommendSimilarTo(title == null ? "" : title);
        String summary = results.isEmpty()
                ? "Nenhum filme parecido com \"" + title + "\" foi encontrado."
                : "Encontrados " + results.size() + " filme(s) parecido(s) com \"" + title + "\".";
        return new CommandResult(!results.isEmpty(), summary, results);
    }
}
