package br.com.fatec.cinemindai.agent.state;

import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;

public class AwaitingHumanApprovalState implements AgentState {

    @Override
    public String name() {
        return "AWAITING_HUMAN_APPROVAL";
    }

    @Override
    public AgentState process(AgentExecutionContext context) {
        if (!context.isApproved()) {
            return this;
        }
        if (context.currentPlan().steps().isEmpty()) {
            return new RespondingState();
        }
        return new ExecutingCommandState();
    }
}
