package br.com.fatec.cinemindai.agent.command;

import br.com.fatec.cinemindai.movie.MovieCatalogPort;
import br.com.fatec.cinemindai.movie.MovieSummary;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendByGenreCommand implements AgentCommand {

    private final MovieCatalogPort catalog;

    public RecommendByGenreCommand(MovieCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public String name() {
        return "recommend_by_genre";
    }

    @Override
    public String description() {
        return "Recomenda filmes de um gênero específico. Argumentos: {\"genre\": string}.";
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String genre = context.argument("genre");
        List<MovieSummary> results = catalog.recommendByGenre(genre == null ? "" : genre);
        String summary = results.isEmpty()
                ? "Nenhuma recomendação encontrada para o gênero \"" + genre + "\"."
                : "Encontradas " + results.size() + " recomendação(ões) para o gênero \"" + genre + "\".";
        return new CommandResult(!results.isEmpty(), summary, results);
    }
}
