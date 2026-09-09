package br.com.fatec.cinemindai.agent.core;

import br.com.fatec.cinemindai.agent.command.CommandExecutionRecord;
import br.com.fatec.cinemindai.agent.command.CommandResult;
import br.com.fatec.cinemindai.agent.observer.AgentExecutionListener;
import br.com.fatec.cinemindai.agent.observer.ExecutionEvent;
import br.com.fatec.cinemindai.agent.observer.ExecutionEventType;
import br.com.fatec.cinemindai.agent.state.AgentState;
import br.com.fatec.cinemindai.agent.state.AwaitingHumanApprovalState;
import br.com.fatec.cinemindai.agent.state.CompletedState;
import br.com.fatec.cinemindai.agent.state.FailedState;
import br.com.fatec.cinemindai.agent.state.PlanningState;
import br.com.fatec.cinemindai.agent.strategy.Plan;
import br.com.fatec.cinemindai.agent.strategy.PlannedStep;
import br.com.fatec.cinemindai.agent.strategy.PlannerMode;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * O "Subject" do padrão OBSERVER e, ao mesmo tempo, o dono do estado atual do padrão STATE:
 * guarda todos os dados de uma execução (mensagem do usuário, plano corrente, resultados de
 * comandos) e conduz o loop {@link #run()} delegando o comportamento a cada {@link AgentState}.
 */
public class AgentExecutionContext {

    public static final int MAX_ITERATIONS = 6;

    private final String conversationId;
    private final String userMessage;
    private final PlannerMode requestedMode;
    private final AgentRuntimeServices services;
    private final List<AgentExecutionListener> listeners;
    private final List<CommandExecutionRecord> commandResults = new CopyOnWriteArrayList<>();

    private AgentState currentState = new PlanningState();
    private Plan currentPlan;
    private PlannerMode resolvedMode;
    private String finalAnswer;
    private boolean approved;
    private int iterationCount;

    public AgentExecutionContext(String conversationId, String userMessage, PlannerMode requestedMode,
            AgentRuntimeServices services, List<AgentExecutionListener> listeners) {
        this.conversationId = conversationId;
        this.userMessage = userMessage;
        this.requestedMode = requestedMode;
        this.services = services;
        this.listeners = listeners;
    }

    /** Executa o loop de estados até chegar a um estado terminal ou pausar aguardando aprovação humana. */
    public void run() {
        while (true) {
            AgentState previous = currentState;
            AgentState next = previous.process(this);
            boolean changed = next != previous;
            currentState = next;
            if (changed) {
                emit(ExecutionEventType.STATE_CHANGED, previous.name() + " -> " + next.name());
            }
            if (currentState instanceof CompletedState || currentState instanceof FailedState) {
                emit(ExecutionEventType.COMPLETED, "Execução finalizada em " + currentState.name());
                return;
            }
            if (currentState instanceof AwaitingHumanApprovalState && !changed) {
                emit(ExecutionEventType.WAITING_APPROVAL, "Aguardando aprovação humana para prosseguir");
                return;
            }
        }
    }

    public void emit(ExecutionEventType type, String message) {
        ExecutionEvent event = new ExecutionEvent(conversationId, type, message, Instant.now());
        for (AgentExecutionListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public void recordCommandResult(PlannedStep step, CommandResult result) {
        commandResults.add(new CommandExecutionRecord(step.commandName(), step.arguments(), result, Instant.now()));
    }

    public void approve() {
        this.approved = true;
    }

    public int incrementAndGetIteration() {
        return ++iterationCount;
    }

    public String conversationId() {
        return conversationId;
    }

    public String userMessage() {
        return userMessage;
    }

    public PlannerMode requestedMode() {
        return requestedMode;
    }

    public AgentRuntimeServices services() {
        return services;
    }

    public AgentState currentState() {
        return currentState;
    }

    public Plan currentPlan() {
        return currentPlan;
    }

    public void setCurrentPlan(Plan plan) {
        this.currentPlan = plan;
    }

    public PlannerMode resolvedMode() {
        return resolvedMode;
    }

    public void setResolvedMode(PlannerMode resolvedMode) {
        this.resolvedMode = resolvedMode;
    }

    public String finalAnswer() {
        return finalAnswer;
    }

    public void setFinalAnswer(String finalAnswer) {
        this.finalAnswer = finalAnswer;
    }

    public boolean isApproved() {
        return approved;
    }

    public List<CommandExecutionRecord> commandResults() {
        return List.copyOf(commandResults);
    }
}
