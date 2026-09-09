package br.com.fatec.cinemindai.agent.strategy;

import java.util.Map;

/**
 * Um passo de execução planejado pelo modelo: qual comando chamar, com quais argumentos,
 * e por qual motivo (útil para depuração e para exibir a trace de execução).
 */
public record PlannedStep(String commandName, Map<String, Object> arguments, String rationale) {

    public PlannedStep {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}
