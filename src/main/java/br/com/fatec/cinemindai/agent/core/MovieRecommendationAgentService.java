package br.com.fatec.cinemindai.agent.core;

import br.com.fatec.cinemindai.agent.command.CommandRegistry;
import br.com.fatec.cinemindai.agent.observer.AgentExecutionListener;
import br.com.fatec.cinemindai.agent.observer.ExecutionTraceRecorder;
import br.com.fatec.cinemindai.agent.state.AwaitingHumanApprovalState;
import br.com.fatec.cinemindai.agent.strategy.PlannerMode;
import br.com.fatec.cinemindai.agent.strategy.PlannerSelector;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Fachada usada pela camada web. Monta um novo {@link AgentExecutionContext} por
 * interação, injetando (Dependency Injection) todos os beans que implementam
 * {@link AgentExecutionListener} como observadores — nenhum listener precisa ser
 * registrado manualmente.
 */
@Service
public class MovieRecommendationAgentService {

    private final PlannerSelector plannerSelector;
    private final CommandRegistry commandRegistry;
    private final ChatClient chatClient;
    private final ConversationSessionStore sessionStore;
    private final List<AgentExecutionListener> listeners;
    private final ExecutionTraceRecorder traceRecorder;

    public MovieRecommendationAgentService(PlannerSelector plannerSelector,
            CommandRegistry commandRegistry,
            ChatClient movieAgentChatClient,
            ConversationSessionStore sessionStore,
            List<AgentExecutionListener> listeners,
            ExecutionTraceRecorder traceRecorder) {
        this.plannerSelector = plannerSelector;
        this.commandRegistry = commandRegistry;
        this.chatClient = movieAgentChatClient;
        this.sessionStore = sessionStore;
        this.listeners = listeners;
        this.traceRecorder = traceRecorder;
    }

    public AgentInteractionResult handleMessage(String conversationId, String message, PlannerMode plannerMode) {
        AgentRuntimeServices services = new AgentRuntimeServices(plannerSelector, commandRegistry, chatClient);
        AgentExecutionContext context = new AgentExecutionContext(
                conversationId, message, plannerMode == null ? PlannerMode.AUTO : plannerMode, services, listeners);
        sessionStore.put(conversationId, context);
        context.run();
        return toResult(context);
    }

    public AgentInteractionResult approve(String conversationId) {
        AgentExecutionContext context = sessionStore.get(conversationId);
        if (context == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada: " + conversationId);
        }
        if (!(context.currentState() instanceof AwaitingHumanApprovalState)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conversa não está aguardando aprovação.");
        }
        context.approve();
        context.run();
        return toResult(context);
    }

    private AgentInteractionResult toResult(AgentExecutionContext context) {
        return new AgentInteractionResult(
                context.conversationId(),
                context.currentState().name(),
                context.finalAnswer(),
                traceRecorder.trace(context.conversationId()));
    }
}
