package br.com.fatec.cinemindai.agent.core;

import br.com.fatec.cinemindai.agent.observer.ExecutionEvent;
import java.util.List;

/**
 * Resultado devolvido pela {@code MovieRecommendationAgentService} para a camada web:
 * estado atual do agente, resposta final (quando disponível) e a trace de eventos
 * capturada pelo {@code ExecutionTraceRecorder} (padrão Observer), útil para demonstrar
 * a arquitetura na apresentação do trabalho.
 */
public record AgentInteractionResult(String conversationId, String state, String reply, List<ExecutionEvent> trace) {
}
