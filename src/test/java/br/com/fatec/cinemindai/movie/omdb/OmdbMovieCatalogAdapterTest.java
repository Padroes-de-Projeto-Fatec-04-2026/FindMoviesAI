package br.com.fatec.cinemindai.movie.omdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.com.fatec.cinemindai.movie.MovieDetails;
import br.com.fatec.cinemindai.movie.MovieSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Testa o adapter OMDb contra respostas HTTP simuladas (sem rede), cobrindo o mapeamento
 * dos campos da OMDb ("N/A", ano em intervalo, listas separadas por vírgula) e a montagem
 * de recomendações por gênero, que a API não oferece nativamente.
 */
class OmdbMovieCatalogAdapterTest {

    private static final String INTERSTELLAR = """
            {"Title":"Interstellar","Year":"2014","Genre":"Adventure, Drama, Sci-Fi",
             "Director":"Christopher Nolan","Actors":"Matthew McConaughey, Anne Hathaway",
             "Plot":"A team travels through a wormhole.","imdbRating":"8.7","imdbID":"tt0816692","Response":"True"}
            """;

    private static final String OFFICE_SPACE = """
            {"Title":"Office Space","Year":"1999","Genre":"Comedy","Director":"Mike Judge","Actors":"Ron Livingston",
             "Plot":"Three company workers hate their jobs.","imdbRating":"7.6","imdbID":"tt0151804","Response":"True"}
            """;

    private static final String NOT_FOUND = """
            {"Response":"False","Error":"Movie not found!"}
            """;

    private MockRestServiceServer server;
    private OmdbMovieCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        OmdbProperties properties = new OmdbProperties("https://www.omdbapi.com/", "test-key", 2, 3);
        adapter = new OmdbMovieCatalogAdapter(new OmdbClient(builder, properties), properties);
    }

    @Test
    void searchByTitleFetchesDetailsForEachResultUpToMaxResults() {
        server.expect(queryParam("s", "interstellar"))
                .andRespond(withSuccess(searchResponse("tt0816692", "tt0151804", "tt9999999"), MediaType.APPLICATION_JSON));
        server.expect(queryParam("i", "tt0816692")).andRespond(withSuccess(INTERSTELLAR, MediaType.APPLICATION_JSON));
        server.expect(queryParam("i", "tt0151804")).andRespond(withSuccess(OFFICE_SPACE, MediaType.APPLICATION_JSON));

        List<MovieSummary> results = adapter.searchByTitle("interstellar");

        assertThat(results).extracting(MovieSummary::title).containsExactly("Interstellar", "Office Space");
        assertThat(results.get(0).year()).isEqualTo(2014);
        assertThat(results.get(0).genres()).containsExactly("Adventure", "Drama", "Sci-Fi");
        server.verify();
    }

    @Test
    void getDetailsMapsFieldsAndUsesTitleLookupForNonImdbIds() {
        server.expect(queryParam("t", "Interstellar")).andRespond(withSuccess(INTERSTELLAR, MediaType.APPLICATION_JSON));

        MovieDetails details = adapter.getDetails("Interstellar");

        assertThat(details.id()).isEqualTo("tt0816692");
        assertThat(details.director()).isEqualTo("Christopher Nolan");
        assertThat(details.cast()).containsExactly("Matthew McConaughey", "Anne Hathaway");
        assertThat(details.rating()).isEqualTo(8.7);
        server.verify();
    }

    @Test
    void getDetailsReturnsNullWhenOmdbAnswersResponseFalse() {
        server.expect(queryParam("t", "Inexistente")).andRespond(withSuccess(NOT_FOUND, MediaType.APPLICATION_JSON));

        assertThat(adapter.getDetails("Inexistente")).isNull();
        server.verify();
    }

    @Test
    void handlesNotAvailableValuesAndYearRanges() {
        String sparse = """
                {"Title":"Obscure","Year":"2014–2019","Genre":"N/A","Director":"N/A","Actors":"N/A",
                 "Plot":"N/A","imdbRating":"N/A","imdbID":"tt0000001","Response":"True"}
                """;
        server.expect(queryParam("i", "tt0000001")).andRespond(withSuccess(sparse, MediaType.APPLICATION_JSON));

        MovieDetails details = adapter.getDetails("tt0000001");

        assertThat(details.year()).isEqualTo(2014);
        assertThat(details.genres()).isEmpty();
        assertThat(details.cast()).isEmpty();
        assertThat(details.synopsis()).isEmpty();
        assertThat(details.rating()).isEqualTo(0.0);
        server.verify();
    }

    @Test
    void recommendByGenreTranslatesPortugueseGenreAndFiltersCandidatesByOmdbGenre() {
        // "ficção científica" -> Sci-Fi; termos de busca: space, alien, star. maxCandidates=3 corta na 1ª busca.
        server.expect(queryParam("s", "space"))
                .andRespond(withSuccess(searchResponse("tt0816692", "tt0151804", "tt0000001"), MediaType.APPLICATION_JSON));
        server.expect(queryParam("i", "tt0816692")).andRespond(withSuccess(INTERSTELLAR, MediaType.APPLICATION_JSON));
        server.expect(queryParam("i", "tt0151804")).andRespond(withSuccess(OFFICE_SPACE, MediaType.APPLICATION_JSON));
        server.expect(queryParam("i", "tt0000001")).andRespond(withSuccess(NOT_FOUND, MediaType.APPLICATION_JSON));

        List<MovieSummary> results = adapter.recommendByGenre("ficção científica");

        assertThat(results).extracting(MovieSummary::title).containsExactly("Interstellar");
        server.verify();
    }

    @Test
    void recommendByGenreReturnsEmptyForUnknownGenreWithoutCallingApi() {
        server.expect(ExpectedCount.never(), queryParam("apikey", "test-key"));

        assertThat(adapter.recommendByGenre("gênero que não existe")).isEmpty();
        server.verify();
    }

    @Test
    void recommendSimilarToUsesReferenceMainGenreAndExcludesReferenceItself() {
        server.expect(queryParam("t", "Interstellar")).andRespond(withSuccess(INTERSTELLAR, MediaType.APPLICATION_JSON));
        // Gênero principal de Interstellar é Adventure -> termos: jones, jurassic, quest.
        server.expect(queryParam("s", "jones"))
                .andRespond(withSuccess(searchResponse("tt0816692", "tt0082971"), MediaType.APPLICATION_JSON));
        server.expect(queryParam("s", "jurassic"))
                .andRespond(withSuccess(searchResponse("tt0107290"), MediaType.APPLICATION_JSON));
        // Ainda abaixo de maxCandidates (3), então o terceiro termo também é consultado.
        server.expect(queryParam("s", "quest")).andRespond(withSuccess(NOT_FOUND, MediaType.APPLICATION_JSON));
        server.expect(queryParam("i", "tt0082971")).andRespond(withSuccess("""
                {"Title":"Raiders of the Lost Ark","Year":"1981","Genre":"Action, Adventure","Director":"Steven Spielberg",
                 "Actors":"Harrison Ford","Plot":"An archaeologist.","imdbRating":"8.4","imdbID":"tt0082971","Response":"True"}
                """, MediaType.APPLICATION_JSON));
        server.expect(queryParam("i", "tt0107290")).andRespond(withSuccess("""
                {"Title":"Jurassic Park","Year":"1993","Genre":"Action, Adventure, Sci-Fi","Director":"Steven Spielberg",
                 "Actors":"Sam Neill","Plot":"Dinosaurs.","imdbRating":"8.2","imdbID":"tt0107290","Response":"True"}
                """, MediaType.APPLICATION_JSON));

        List<MovieSummary> results = adapter.recommendSimilarTo("Interstellar");

        assertThat(results).extracting(MovieSummary::id).containsExactly("tt0082971", "tt0107290");
        server.verify();
    }

    private static String searchResponse(String... imdbIds) {
        StringBuilder items = new StringBuilder();
        for (String id : imdbIds) {
            if (!items.isEmpty()) {
                items.append(',');
            }
            items.append("{\"Title\":\"Movie ").append(id).append("\",\"Year\":\"2000\",\"imdbID\":\"").append(id)
                    .append("\",\"Type\":\"movie\",\"Poster\":\"N/A\"}");
        }
        return "{\"Search\":[" + items + "],\"totalResults\":\"" + imdbIds.length + "\",\"Response\":\"True\"}";
    }
}
