package br.com.fatec.cinemindai.agent.command;

import br.com.fatec.cinemindai.agent.strategy.PlannedStep;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Invoker do padrão COMMAND: descobre todos os {@link AgentCommand} registrados como
 * beans Spring (Dependency Injection), despacha por nome e mantém o histórico de execução.
 */
@Component
public class CommandRegistry {

    private final Map<String, AgentCommand> commandsByName;
    private final List<CommandExecutionRecord> history = new CopyOnWriteArrayList<>();

    public CommandRegistry(List<AgentCommand> commands) {
        this.commandsByName = commands.stream()
                .collect(Collectors.toUnmodifiableMap(AgentCommand::name, command -> command));
    }

    public CommandResult execute(PlannedStep step) {
        AgentCommand command = commandsByName.get(step.commandName());
        CommandResult result = command == null
                ? new CommandResult(false, "Comando desconhecido: " + step.commandName(), null)
                : command.execute(new CommandContext(step.arguments()));
        history.add(new CommandExecutionRecord(step.commandName(), step.arguments(), result, Instant.now()));
        return result;
    }

    public List<CommandExecutionRecord> history() {
        return List.copyOf(history);
    }

    public String describeAvailableCommands() {
        return commandsByName.values().stream()
                .map(command -> "- " + command.name() + ": " + command.description())
                .collect(Collectors.joining("\n"));
    }
}
