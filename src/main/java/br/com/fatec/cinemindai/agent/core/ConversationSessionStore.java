package br.com.fatec.cinemindai.agent.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Guarda o contexto de execução em andamento de cada conversa (em memória, suficiente
 * para este projeto acadêmico) para permitir retomar após uma pausa de aprovação humana.
 */
@Component
public class ConversationSessionStore {

    private final Map<String, AgentExecutionContext> sessions = new ConcurrentHashMap<>();

    public void put(String conversationId, AgentExecutionContext context) {
        sessions.put(conversationId, context);
    }

    public AgentExecutionContext get(String conversationId) {
        return sessions.get(conversationId);
    }
}
