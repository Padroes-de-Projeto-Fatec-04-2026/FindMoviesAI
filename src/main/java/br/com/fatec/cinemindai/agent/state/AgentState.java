package br.com.fatec.cinemindai.agent.state;

import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;

/**
 * Padrão STATE: o ciclo de vida da execução do agente (PLANNING -> EXECUTING_COMMAND ->
 * RESPONDING -> COMPLETED, com desvios para AWAITING_HUMAN_APPROVAL / FAILED) é modelado
 * como uma máquina de estados explícita, evitando if/else espalhados e impedindo estados
 * ilegais (ex.: executar um comando antes de existir um plano).
 */
public interface AgentState {

    String name();

    /** Processa este estado e retorna o próximo estado (pode ser {@code this} para permanecer parado). */
    AgentState process(AgentExecutionContext context);
}
