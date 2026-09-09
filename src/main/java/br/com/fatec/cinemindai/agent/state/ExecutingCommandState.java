package br.com.fatec.cinemindai.agent.state;

import br.com.fatec.cinemindai.agent.command.CommandResult;
import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;
import br.com.fatec.cinemindai.agent.observer.ExecutionEventType;
import br.com.fatec.cinemindai.agent.strategy.Plan;
import br.com.fatec.cinemindai.agent.strategy.PlannedStep;
import br.com.fatec.cinemindai.agent.strategy.PlannerMode;

public class ExecutingCommandState implements AgentState {

    @Override
    public String name() {
        return "EXECUTING_COMMAND";
    }

    @Override
    public AgentState process(AgentExecutionContext context) {
        Plan plan = context.currentPlan();
        for (PlannedStep step : plan.steps()) {
            CommandResult result = context.services().commandRegistry().execute(step);
            context.recordCommandResult(step, result);
            context.emit(ExecutionEventType.COMMAND_EXECUTED, step.commandName() + " -> " + result.summary());
        }

        if (context.resolvedMode() == PlannerMode.REACT) {
            // ReAct: volta a planejar o próximo passo com base nas novas observações.
            return new PlanningState();
        }
        return new RespondingState();
    }
}
