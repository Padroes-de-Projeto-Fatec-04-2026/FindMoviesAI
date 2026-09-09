package br.com.fatec.cinemindai.agent.observer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Observer que empurra eventos de execução em tempo real para clientes inscritos via SSE,
 * demonstrando o benefício de "dashboards/feedback em tempo real" citado no padrão Observer.
 */
@Component
public class SseAgentEventListener implements AgentExecutionListener {

    private final Map<String, List<SseEmitter>> emittersByConversation = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String conversationId) {
        SseEmitter emitter = new SseEmitter(0L);
        emittersByConversation.computeIfAbsent(conversationId, id -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(conversationId, emitter));
        emitter.onTimeout(() -> removeEmitter(conversationId, emitter));
        return emitter;
    }

    @Override
    public void onEvent(ExecutionEvent event) {
        List<SseEmitter> subscribers = emittersByConversation.get(event.conversationId());
        if (subscribers == null) {
            return;
        }
        for (SseEmitter emitter : subscribers) {
            try {
                emitter.send(SseEmitter.event().name(event.type().name()).data(event));
            } catch (IOException ex) {
                removeEmitter(event.conversationId(), emitter);
            }
        }
    }

    private void removeEmitter(String conversationId, SseEmitter emitter) {
        List<SseEmitter> subscribers = emittersByConversation.get(conversationId);
        if (subscribers != null) {
            subscribers.remove(emitter);
        }
    }
}
