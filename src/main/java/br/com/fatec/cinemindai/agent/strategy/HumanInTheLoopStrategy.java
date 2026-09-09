package br.com.fatec.cinemindai.agent.strategy;

import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;
import org.springframework.stereotype.Component;

/**
 * Decora {@link PlanThenExecuteStrategy}: gera o plano normalmente, mas exige aprovação
 * humana explícita antes de executar qualquer comando (útil quando o usuário pede
 * confirmação passo a passo, ex.: "me confirme antes de buscar").
 */
@Component
public class HumanInTheLoopStrategy implements PlanningStrategy {

    private final PlanThenExecuteStrategy delegate;

    public HumanInTheLoopStrategy(PlanThenExecuteStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public PlannerMode mode() {
        return PlannerMode.HUMAN_IN_THE_LOOP;
    }

    @Override
    public Plan plan(AgentExecutionContext context) {
        Plan basePlan = delegate.plan(context);
        return basePlan.hasFinalAnswer() ? basePlan : basePlan.withHumanApproval();
    }
}
