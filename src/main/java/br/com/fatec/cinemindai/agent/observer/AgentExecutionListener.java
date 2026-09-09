package br.com.fatec.cinemindai.agent.observer;

/**
 * Padrão OBSERVER: qualquer bean Spring que implemente esta interface é automaticamente
 * injetado (como {@code List<AgentExecutionListener>}) e passa a receber, em tempo real,
 * cada evento relevante do ciclo de execução do agente (mudança de estado, plano criado,
 * comando executado, erro). Permite plugar logging, dashboards, alertas ou SSE sem tocar
 * no runtime do agente.
 */
public interface AgentExecutionListener {

    void onEvent(ExecutionEvent event);
}
