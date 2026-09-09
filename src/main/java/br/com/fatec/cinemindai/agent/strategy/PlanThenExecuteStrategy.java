package br.com.fatec.cinemindai.agent.strategy;

import br.com.fatec.cinemindai.agent.command.CommandRegistry;
import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

/**
 * Pede ao modelo, em uma única chamada, um plano completo de comandos a executar
 * antes de qualquer execução. Bom para pedidos diretos ("recomende filmes de terror").
 */
@Component
public class PlanThenExecuteStrategy implements PlanningStrategy {

    private final CommandRegistry commandRegistry;

    public PlanThenExecuteStrategy(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public PlannerMode mode() {
        return PlannerMode.PLAN_THEN_EXECUTE;
    }

    @Override
    public Plan plan(AgentExecutionContext context) {
        ChatClient chatClient = context.services().chatClient();
        String systemPrompt = """
                Você é o planejador de um agente de recomendação de filmes.
                Antes de responder, gere um plano completo com todos os comandos necessários.
                Comandos disponíveis:
                %s
                Regras:
                - Se nenhum comando for necessário para responder, deixe "steps" vazio e preencha "finalAnswer".
                - Se comandos forem necessários, preencha "steps" e deixe "finalAnswer" vazio (ele será gerado depois, com os resultados).
                - Responda SOMENTE no formato estruturado pedido.
                """.formatted(commandRegistry.describeAvailableCommands());

        LlmPlan llmPlan = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, context.conversationId()))
                .system(systemPrompt)
                .user(context.userMessage())
                .call()
                .entity(LlmPlan.class);

        if (llmPlan == null) {
            return new Plan(List.of(), "Não consegui montar um plano para esse pedido. Pode reformular?", false);
        }
        return new Plan(llmPlan.steps(), llmPlan.finalAnswer(), false);
    }
}
