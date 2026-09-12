package br.com.fatec.cinemindai.agent.command;

import br.com.fatec.cinemindai.movie.MovieCatalogPort;
import br.com.fatec.cinemindai.movie.MovieDetails;
import org.springframework.stereotype.Component;

@Component
public class GetMovieDetailsCommand implements AgentCommand {

    private final MovieCatalogPort catalog;

    public GetMovieDetailsCommand(MovieCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public String name() {
        return "get_movie_details";
    }

    @Override
    public String description() {
        return "Obtém detalhes completos de um filme (sinopse, diretor, elenco, nota). "
                + "Argumentos: {\"titleOrId\": string, título original em inglês ou IMDb ID como tt0816692}.";
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String titleOrId = context.argument("titleOrId");
        MovieDetails details = catalog.getDetails(titleOrId == null ? "" : titleOrId);
        if (details == null) {
            return new CommandResult(false, "Filme não encontrado: \"" + titleOrId + "\".", null);
        }
        return new CommandResult(true, "Detalhes encontrados para \"" + details.title() + "\".", details);
    }
}
