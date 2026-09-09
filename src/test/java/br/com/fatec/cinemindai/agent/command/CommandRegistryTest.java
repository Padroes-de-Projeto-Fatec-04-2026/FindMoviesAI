package br.com.fatec.cinemindai.agent.command;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fatec.cinemindai.agent.strategy.PlannedStep;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommandRegistryTest {

    private final AgentCommand echoCommand = new AgentCommand() {
        @Override
        public String name() {
            return "echo";
        }

        @Override
        public String description() {
            return "Ecoa o argumento 'text'.";
        }

        @Override
        public CommandResult execute(CommandContext context) {
            return new CommandResult(true, "eco: " + context.argument("text"), null);
        }
    };

    @Test
    void dispatchesToRegisteredCommandByName() {
        CommandRegistry registry = new CommandRegistry(List.of(echoCommand));

        CommandResult result = registry.execute(new PlannedStep("echo", Map.of("text", "ola"), "teste"));

        assertThat(result.success()).isTrue();
        assertThat(result.summary()).isEqualTo("eco: ola");
        assertThat(registry.history()).hasSize(1);
    }

    @Test
    void returnsFailureResultForUnknownCommand() {
        CommandRegistry registry = new CommandRegistry(List.of());

        CommandResult result = registry.execute(new PlannedStep("inexistente", Map.of(), "teste"));

        assertThat(result.success()).isFalse();
        assertThat(registry.history()).hasSize(1);
    }

    @Test
    void describesAvailableCommandsForThePlanningPrompt() {
        CommandRegistry registry = new CommandRegistry(List.of(echoCommand));

        assertThat(registry.describeAvailableCommands()).contains("echo");
    }
}
