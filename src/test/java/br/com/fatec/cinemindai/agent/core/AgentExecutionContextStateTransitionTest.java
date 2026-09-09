package br.com.fatec.cinemindai.agent.core;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fatec.cinemindai.agent.command.AgentCommand;
import br.com.fatec.cinemindai.agent.command.CommandContext;
import br.com.fatec.cinemindai.agent.command.CommandRegistry;
import br.com.fatec.cinemindai.agent.command.CommandResult;
import br.com.fatec.cinemindai.agent.observer.AgentExecutionListener;
import br.com.fatec.cinemindai.agent.observer.ExecutionEvent;
import br.com.fatec.cinemindai.agent.observer.ExecutionEventType;
import br.com.fatec.cinemindai.agent.state.AwaitingHumanApprovalState;
import br.com.fatec.cinemindai.agent.state.CompletedState;
import br.com.fatec.cinemindai.agent.strategy.Plan;
import br.com.fatec.cinemindai.agent.strategy.PlannedStep;
import br.com.fatec.cinemindai.agent.strategy.PlannerMode;
import br.com.fatec.cinemindai.agent.strategy.PlannerSelector;
import br.com.fatec.cinemindai.agent.strategy.PlanningStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Valida o padrão STATE (transições PLANNING -> EXECUTING_COMMAND -> RESPONDING -> COMPLETED,
 * e a pausa em AWAITING_HUMAN_APPROVAL) e o padrão OBSERVER (eventos emitidos durante o loop),
 * usando um {@link PlanningStrategy} e um {@link AgentCommand} fakes para não depender de um
 * Ollama real rodando.
 */
class AgentExecutionContextStateTransitionTest {

    private final AgentCommand noopCommand = new AgentCommand() {
        @Override
        public String name() {
            return "noop";
        }

        @Override
        public String description() {
            return "comando de teste";
        }

        @Override
        public CommandResult execute(CommandContext context) {
            return new CommandResult(true, "ok", null);
        }
    };

    @Test
    void runsThroughPlanningExecutingRespondingAndCompleted() {
        List<ExecutionEvent> capturedEvents = new ArrayList<>();
        AgentExecutionListener listener = capturedEvents::add;

        CommandRegistry commandRegistry = new CommandRegistry(List.of(noopCommand));
        PlanningStrategy fakeStrategy = new PlanningStrategy() {
            @Override
            public PlannerMode mode() {
                return PlannerMode.PLAN_THEN_EXECUTE;
            }

            @Override
            public Plan plan(AgentExecutionContext context) {
                return new Plan(List.of(new PlannedStep("noop", Map.of(), "teste")), null, false);
            }
        };
        PlannerSelector fakeSelector = new PlannerSelector(null, null, null) {
            @Override
            public PlanningStrategy select(PlannerMode requestedMode, String userMessage) {
                return fakeStrategy;
            }
        };

        AgentRuntimeServices services = new AgentRuntimeServices(fakeSelector, commandRegistry, null);
        AgentExecutionContext context = new AgentExecutionContext(
                "conv-1", "recomende um filme", PlannerMode.AUTO, services, List.of(listener));
        // Evita a chamada ao ChatClient (não disponível neste teste): a RespondingState só
        // sintetiza uma resposta via LLM quando finalAnswer ainda está em branco.
        context.setFinalAnswer("Resposta pré-definida para o teste");

        context.run();

        assertThat(context.currentState()).isInstanceOf(CompletedState.class);
        assertThat(context.commandResults()).hasSize(1);
        assertThat(capturedEvents).extracting(ExecutionEvent::type)
                .contains(ExecutionEventType.PLAN_CREATED, ExecutionEventType.COMMAND_EXECUTED, ExecutionEventType.COMPLETED);
    }

    @Test
    void pausesForHumanApprovalAndResumesAfterApprove() {
        CommandRegistry commandRegistry = new CommandRegistry(List.of(noopCommand));
        PlanningStrategy fakeStrategy = new PlanningStrategy() {
            @Override
            public PlannerMode mode() {
                return PlannerMode.HUMAN_IN_THE_LOOP;
            }

            @Override
            public Plan plan(AgentExecutionContext context) {
                return new Plan(List.of(new PlannedStep("noop", Map.of(), "teste")), null, true);
            }
        };
        PlannerSelector fakeSelector = new PlannerSelector(null, null, null) {
            @Override
            public PlanningStrategy select(PlannerMode requestedMode, String userMessage) {
                return fakeStrategy;
            }
        };

        AgentRuntimeServices services = new AgentRuntimeServices(fakeSelector, commandRegistry, null);
        AgentExecutionContext context = new AgentExecutionContext(
                "conv-2", "confirma antes de buscar", PlannerMode.AUTO, services, List.of());
        context.setFinalAnswer("resposta pronta");

        context.run();
        assertThat(context.currentState()).isInstanceOf(AwaitingHumanApprovalState.class);
        assertThat(context.commandResults()).isEmpty();

        context.approve();
        context.run();

        assertThat(context.currentState()).isInstanceOf(CompletedState.class);
        assertThat(context.commandResults()).hasSize(1);
    }
}
