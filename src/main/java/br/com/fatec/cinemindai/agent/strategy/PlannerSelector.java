package br.com.fatec.cinemindai.agent.strategy;

import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Escolhe qual {@link PlanningStrategy} usar: respeita um modo explícito pedido pelo
 * cliente da API e, quando o modo é {@link PlannerMode#AUTO}, aplica uma heurística
 * simples baseada na mensagem do usuário.
 */
@Component
public class PlannerSelector {

    private final ReActPlanningStrategy reActPlanningStrategy;
    private final PlanThenExecuteStrategy planThenExecuteStrategy;
    private final HumanInTheLoopStrategy humanInTheLoopStrategy;

    public PlannerSelector(ReActPlanningStrategy reActPlanningStrategy,
            PlanThenExecuteStrategy planThenExecuteStrategy,
            HumanInTheLoopStrategy humanInTheLoopStrategy) {
        this.reActPlanningStrategy = reActPlanningStrategy;
        this.planThenExecuteStrategy = planThenExecuteStrategy;
        this.humanInTheLoopStrategy = humanInTheLoopStrategy;
    }

    public PlanningStrategy select(PlannerMode requestedMode, String userMessage) {
        PlannerMode resolved = requestedMode == null || requestedMode == PlannerMode.AUTO
                ? heuristic(userMessage)
                : requestedMode;
        return switch (resolved) {
            case REACT -> reActPlanningStrategy;
            case HUMAN_IN_THE_LOOP -> humanInTheLoopStrategy;
            case PLAN_THEN_EXECUTE, AUTO -> planThenExecuteStrategy;
        };
    }

    private PlannerMode heuristic(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return PlannerMode.PLAN_THEN_EXECUTE;
        }
        String lower = userMessage.toLowerCase(Locale.ROOT);
        if (lower.contains("confirma") || lower.contains("passo a passo")) {
            return PlannerMode.HUMAN_IN_THE_LOOP;
        }
        if (userMessage.split("\\s+").length > 25) {
            return PlannerMode.REACT;
        }
        return PlannerMode.PLAN_THEN_EXECUTE;
    }
}
