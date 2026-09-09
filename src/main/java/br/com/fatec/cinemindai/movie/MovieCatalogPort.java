package br.com.fatec.cinemindai.movie;

import java.util.List;

/**
 * Ponto de integração com a fonte real de dados de filmes (API externa e/ou banco de dados),
 * responsabilidade de outro integrante do grupo. A implementação padrão do projeto
 * ({@link InMemoryMovieCatalogAdapter}) é apenas um stub para permitir que o agente
 * (State, Command, Strategy, Observer) rode de ponta a ponta enquanto a integração real
 * não é entregue. Basta trocar o bean implementando esta interface pela integração de verdade.
 */
public interface MovieCatalogPort {

    List<MovieSummary> searchByTitle(String title);

    List<MovieSummary> recommendByGenre(String genre);

    List<MovieSummary> recommendSimilarTo(String title);

    MovieDetails getDetails(String titleOrId);
}
