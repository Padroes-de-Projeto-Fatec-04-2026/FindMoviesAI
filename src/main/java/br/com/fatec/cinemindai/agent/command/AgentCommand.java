package br.com.fatec.cinemindai.agent.command;

/**
 * Padrão COMMAND: cada ação concreta que o agente pode executar (buscar filme,
 * recomendar por gênero, etc.) é encapsulada como um comando autocontido, permitindo
 * despacho uniforme, logging e histórico via {@link CommandRegistry}.
 */
public interface AgentCommand {

    String name();

    String description();

    CommandResult execute(CommandContext context);
}
