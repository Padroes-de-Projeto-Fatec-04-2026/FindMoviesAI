package br.com.fatec.cinemindai.agent.state;

import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;
import br.com.fatec.cinemindai.agent.observer.ExecutionEventType;
import br.com.fatec.cinemindai.agent.strategy.Plan;
import br.com.fatec.cinemindai.agent.strategy.PlanningStrategy;

public class PlanningState implements AgentState {

    @Override
    public String name() {
        return "PLANNING";
    }

    @Override
    public AgentState process(AgentExecutionContext context) {
        if (context.incrementAndGetIteration() > AgentExecutionContext.MAX_ITERATIONS) {
            context.setFinalAnswer("Não consegui concluir a recomendação dentro do número máximo de passos permitido.");
            return new FailedState("Limite de iterações do planejador excedido");
        }

        PlanningStrategy strategy = context.services().plannerSelector()
                .select(context.requestedMode(), context.userMessage());
        context.setResolvedMode(strategy.mode());

        Plan plan = strategy.plan(context);
        context.setCurrentPlan(plan);
        context.emit(ExecutionEventType.PLAN_CREATED,
                "Plano gerado via " + strategy.mode() + " com " + plan.steps().size() + " passo(s)");

        if (plan.requiresHumanApproval() && !context.isApproved()) {
            return new AwaitingHumanApprovalState();
        }
        if (plan.steps().isEmpty()) {
            if (plan.hasFinalAnswer()) {
                context.setFinalAnswer(plan.finalAnswer());
            }
            return new RespondingState();
        }
        return new ExecutingCommandState();
    }
}
