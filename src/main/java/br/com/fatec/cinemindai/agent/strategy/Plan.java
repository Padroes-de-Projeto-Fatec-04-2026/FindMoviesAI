package br.com.fatec.cinemindai.agent.strategy;

import java.util.List;

public record Plan(List<PlannedStep> steps, String finalAnswer, boolean requiresHumanApproval) {

    public Plan {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }

    public boolean hasFinalAnswer() {
        return finalAnswer != null && !finalAnswer.isBlank();
    }

    public Plan withHumanApproval() {
        return new Plan(steps, finalAnswer, true);
    }
}
