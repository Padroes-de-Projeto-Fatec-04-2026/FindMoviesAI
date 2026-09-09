package br.com.fatec.cinemindai.agent.core;

import br.com.fatec.cinemindai.agent.command.CommandRegistry;
import br.com.fatec.cinemindai.agent.strategy.PlannerSelector;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Feixe de dependências que os {@code AgentState} precisam para operar sobre o
 * {@link AgentExecutionContext}, evitando que cada estado declare seu próprio conjunto
 * de beans Spring.
 */
public record AgentRuntimeServices(PlannerSelector plannerSelector, CommandRegistry commandRegistry, ChatClient chatClient) {
}
