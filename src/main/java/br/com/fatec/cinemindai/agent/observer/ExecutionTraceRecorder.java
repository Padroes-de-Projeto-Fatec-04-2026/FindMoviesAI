package br.com.fatec.cinemindai.agent.observer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * Observer que guarda a linha do tempo de eventos por conversa, servindo de "dashboard"
 * simples: a trace completa é devolvida junto da resposta da API para fins de demonstração
 * dos padrões usados.
 */
@Component
public class ExecutionTraceRecorder implements AgentExecutionListener {

    private final Map<String, List<ExecutionEvent>> traces = new ConcurrentHashMap<>();

    @Override
    public void onEvent(ExecutionEvent event) {
        traces.computeIfAbsent(event.conversationId(), id -> new CopyOnWriteArrayList<>()).add(event);
    }

    public List<ExecutionEvent> trace(String conversationId) {
        return traces.getOrDefault(conversationId, List.of());
    }
}
