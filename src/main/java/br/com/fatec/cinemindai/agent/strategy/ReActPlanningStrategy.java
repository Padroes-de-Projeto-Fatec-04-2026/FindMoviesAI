package br.com.fatec.cinemindai.agent.strategy;

import br.com.fatec.cinemindai.agent.command.CommandExecutionRecord;
import br.com.fatec.cinemindai.agent.command.CommandRegistry;
import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Planeja UM passo por vez, olhando para as observações (resultados de comandos)
 * já coletadas nesta conversa antes de decidir o próximo passo — o clássico loop
 * "reason + act" do ReAct. É o {@link br.com.fatec.cinemindai.agent.core.AgentExecutionContext}
 * (via o padrão State) quem cuida de repetir esta estratégia até haver uma resposta final.
 */
@Component
public class ReActPlanningStrategy implements PlanningStrategy {

    private final CommandRegistry commandRegistry;

    public ReActPlanningStrategy(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public PlannerMode mode() {
        return PlannerMode.REACT;
    }

    @Override
    public Plan plan(AgentExecutionContext context) {
        ChatClient chatClient = context.services().chatClient();
        String systemPrompt = """
                Você é o planejador de um agente de recomendação de filmes, operando no modo ReAct
                (raciocinar e agir um passo de cada vez).
                Comandos disponíveis:
                %s
                Regras:
                - Analise as observações já coletadas (resultados de comandos anteriores) antes de decidir.
                - Se ainda precisar de mais informação, retorne EXATAMENTE 1 item em "steps" e deixe "finalAnswer" vazio.
                - Se já tem informação suficiente para responder, deixe "steps" vazio e preencha "finalAnswer".
                - Nunca repita um comando com os mesmos argumentos que já foi executado.
                """.formatted(commandRegistry.describeAvailableCommands());

        String observations = describeObservations(context.commandResults());

        LlmPlan llmPlan = chatClient.prompt()
                .system(systemPrompt)
                .user("Pedido original: " + context.userMessage() + "\n\nObservações até agora:\n" + observations)
                .call()
                .entity(LlmPlan.class);

        if (llmPlan == null) {
            return new Plan(List.of(), "Não consegui decidir o próximo passo. Pode reformular o pedido?", false);
        }
        List<PlannedStep> steps = llmPlan.steps().isEmpty() ? List.of() : List.of(llmPlan.steps().get(0));
        return new Plan(steps, llmPlan.finalAnswer(), false);
    }

    private String describeObservations(List<CommandExecutionRecord> history) {
        if (history.isEmpty()) {
            return "(nenhuma ainda)";
        }
        return history.stream()
                .map(record -> "- " + record.commandName() + "(" + record.arguments() + ") -> " + record.result().summary())
                .collect(Collectors.joining("\n"));
    }
}
