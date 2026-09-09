package br.com.fatec.cinemindai.agent.state;

import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;

public class FailedState implements AgentState {

    private final String reason;

    public FailedState(String reason) {
        this.reason = reason;
    }

    public String reason() {
        return reason;
    }

    @Override
    public String name() {
        return "FAILED";
    }

    @Override
    public AgentState process(AgentExecutionContext context) {
        return this;
    }
}
