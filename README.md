# FindMoviesAI

Agente de recomendação de filmes construído com **Spring AI** + **Ollama** (modelo local,
100% open source), demonstrando os padrões de projeto **State**, **Command**, **Strategy** e
**Observer**. Veja a explicação detalhada da arquitetura, com diagramas UML, em
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

> Os dados de filmes vêm da API pública [OMDb](https://www.omdbapi.com/) através do
> `OmdbMovieCatalogAdapter` (`movie.catalog=omdb`). Sem chave de API o projeto usa um stub em
> memória (`InMemoryMovieCatalogAdapter`, `movie.catalog=memory`, padrão) para rodar de ponta a ponta.

## Pré-requisitos

1. [Ollama](https://ollama.com/) instalado e rodando localmente:
   ```bash
   ollama serve
   ollama pull llama3.1
   ```
2. Java 17+ e Maven 3.9+ (`mvn -version`).
3. (Opcional) Chave gratuita da OMDb em https://www.omdbapi.com/apikey.aspx, para usar dados
   reais de filmes em vez do stub em memória.

## Rodando o projeto

Com o stub em memória (não precisa de chave):
```bash
mvn spring-boot:run
```

Com a API OMDb (PowerShell):
```powershell
$env:MOVIE_CATALOG = "omdb"
$env:OMDB_API_KEY = "sua-chave"
mvn spring-boot:run
```
(No bash/Linux: `MOVIE_CATALOG=omdb OMDB_API_KEY=sua-chave mvn spring-boot:run`.)

A aplicação sobe em `http://localhost:8080`. O modelo/URL do Ollama e os limites de consulta à
OMDb (`omdb.max-results`, `omdb.max-candidates`) podem ser ajustados em
`src/main/resources/application.properties`. **Nunca commite a chave** — ela é lida da variável
de ambiente `OMDB_API_KEY`.

> Dica: como a OMDb é em inglês, o agente funciona melhor com títulos originais
> ("Interstellar", não "Interestelar"). No Windows, se o `curl` do Git Bash devolver `400` com
> acentos na mensagem, salve o JSON em um arquivo UTF-8 e envie com `--data-binary @arquivo.json`,
> ou use o Postman.

## Testando via curl

**Pedido direto (Plan-then-Execute, escolhido automaticamente):**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Recomende filmes de ficção científica parecidos com Interestelar"}'
```

**Pedido pedindo confirmação (Human-in-the-loop):**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId": "demo-1", "message": "Pode confirmar antes de buscar filmes de terror?"}'

# resposta virá com "state": "AWAITING_HUMAN_APPROVAL" — aprove com:
curl -X POST http://localhost:8080/api/agent/chat/demo-1/approve
```

**Forçando um modo específico de planejamento:**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Quero algo pra assistir hoje", "plannerMode": "REACT"}'
```
(`plannerMode` aceita `AUTO`, `REACT`, `PLAN_THEN_EXECUTE`, `HUMAN_IN_THE_LOOP`.)

**Acompanhando a execução em tempo real (Observer via SSE):**
```bash
curl -N http://localhost:8080/api/agent/chat/demo-1/events
```

## Testes automatizados

```bash
./mvnw test
```

