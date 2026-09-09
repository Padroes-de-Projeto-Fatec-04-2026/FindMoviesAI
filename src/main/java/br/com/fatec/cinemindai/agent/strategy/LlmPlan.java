package br.com.fatec.cinemindai.agent.strategy;

import java.util.List;

/**
 * Forma de saída estruturada que pedimos ao modelo (via {@code ChatClient...call().entity(LlmPlan.class)}).
 * Deliberadamente não inclui {@code requiresHumanApproval}: essa decisão é tomada pelo
 * {@link HumanInTheLoopStrategy}, não pelo modelo.
 */
public record LlmPlan(List<PlannedStep> steps, String finalAnswer) {

    public LlmPlan {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
