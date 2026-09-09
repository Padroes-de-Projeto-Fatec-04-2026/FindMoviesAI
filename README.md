# FindMoviesAI

Agente de recomendação de filmes construído com **Spring AI** + **Ollama** (modelo local,
100% open source), demonstrando os padrões de projeto **State**, **Command**, **Strategy** e
**Observer**. Veja a explicação detalhada da arquitetura, com diagramas UML, em
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

> A consulta real a dados/API de filmes (ex.: TMDB/OMDb) ainda não realizada. Este módulo já expõe o ponto de integração (`MovieCatalogPort`) e usa,
> por enquanto, uma implementação stub em memória (`InMemoryMovieCatalogAdapter`) para rodar
> de ponta a ponta.

## Pré-requisitos

1. [Ollama](https://ollama.com/) instalado e rodando localmente:
   ```bash
   ollama serve
   ollama pull llama3.1
   ```
2. Java 17+ e o Maven Wrapper incluso no projeto (não precisa instalar Maven).

## Rodando o projeto

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. O modelo/URL do Ollama podem ser ajustados em
`src/main/resources/application.properties`.

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

Os testes em `src/test/java/.../agent/` cobrem o `CommandRegistry`, o `PlannerSelector` e as
transições de estado do `AgentExecutionContext` com fakes — não dependem do Ollama estar
rodando. O teste `CineMindAiApplicationTests` sobe o contexto Spring completo (requer que as
dependências resolvam normalmente; não faz chamadas ao Ollama).
