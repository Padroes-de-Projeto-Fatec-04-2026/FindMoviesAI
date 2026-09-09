package br.com.fatec.cinemindai.agent.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fatec.cinemindai.agent.command.CommandRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlannerSelectorTest {

    private final CommandRegistry commandRegistry = new CommandRegistry(List.of());
    private final PlanThenExecuteStrategy planThenExecute = new PlanThenExecuteStrategy(commandRegistry);
    private final ReActPlanningStrategy reAct = new ReActPlanningStrategy(commandRegistry);
    private final HumanInTheLoopStrategy humanInTheLoop = new HumanInTheLoopStrategy(planThenExecute);
    private final PlannerSelector selector = new PlannerSelector(reAct, planThenExecute, humanInTheLoop);

    @Test
    void respectsAnExplicitlyRequestedMode() {
        assertThat(selector.select(PlannerMode.REACT, "qualquer coisa")).isSameAs(reAct);
        assertThat(selector.select(PlannerMode.HUMAN_IN_THE_LOOP, "qualquer coisa")).isSameAs(humanInTheLoop);
    }

    @Test
    void picksHumanInTheLoopWhenUserAsksForConfirmation() {
        assertThat(selector.select(PlannerMode.AUTO, "pode confirmar antes de buscar?")).isSameAs(humanInTheLoop);
    }

    @Test
    void picksReActForLongOpenEndedMessages() {
        String longMessage = "Eu gostaria de assistir algo hoje a noite mas nao sei bem o que, "
                + "talvez algo de ficcao cientifica ou suspense, dependendo do clima da noite";

        assertThat(selector.select(PlannerMode.AUTO, longMessage)).isSameAs(reAct);
    }

    @Test
    void defaultsToPlanThenExecuteForShortDirectRequests() {
        assertThat(selector.select(PlannerMode.AUTO, "recomende filmes de terror")).isSameAs(planThenExecute);
    }
}
