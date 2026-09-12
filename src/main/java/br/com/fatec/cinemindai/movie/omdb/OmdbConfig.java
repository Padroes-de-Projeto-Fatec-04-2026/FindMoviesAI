package br.com.fatec.cinemindai.movie.omdb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Registra o adapter OMDb como o {@link br.com.fatec.cinemindai.movie.MovieCatalogPort} da
 * aplicação quando {@code movie.catalog=omdb}. Sem essa propriedade o stub em memória
 * continua valendo (útil para testes e para quem não tem chave da OMDb).
 */
@Configuration
@ConditionalOnProperty(name = "movie.catalog", havingValue = "omdb")
@EnableConfigurationProperties(OmdbProperties.class)
public class OmdbConfig {

    @Bean
    public OmdbClient omdbClient(RestClient.Builder restClientBuilder, OmdbProperties properties) {
        return new OmdbClient(restClientBuilder, properties);
    }

    @Bean
    public OmdbMovieCatalogAdapter omdbMovieCatalogAdapter(OmdbClient client, OmdbProperties properties) {
        return new OmdbMovieCatalogAdapter(client, properties);
    }
}
