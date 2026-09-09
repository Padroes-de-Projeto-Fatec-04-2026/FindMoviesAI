package br.com.fatec.cinemindai.agent.state;

import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;

public class CompletedState implements AgentState {

    @Override
    public String name() {
        return "COMPLETED";
    }

    @Override
    public AgentState process(AgentExecutionContext context) {
        return this;
    }
}
