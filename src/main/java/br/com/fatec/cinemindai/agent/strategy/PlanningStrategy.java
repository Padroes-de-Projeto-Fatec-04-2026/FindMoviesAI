package br.com.fatec.cinemindai.agent.strategy;

import br.com.fatec.cinemindai.agent.core.AgentExecutionContext;

/**
 * Padrão STRATEGY: os diferentes modos de planejamento (ReAct, Plan-then-Execute,
 * Human-in-the-loop) implementam esta interface e são intercambiáveis em tempo de
 * execução via {@link PlannerSelector}, sem alterar o restante do runtime do agente.
 */
public interface PlanningStrategy {

    PlannerMode mode();

    Plan plan(AgentExecutionContext context);
}
