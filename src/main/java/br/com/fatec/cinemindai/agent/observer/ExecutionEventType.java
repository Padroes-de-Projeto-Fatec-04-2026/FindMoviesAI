package br.com.fatec.cinemindai.agent.observer;

public enum ExecutionEventType {
    STATE_CHANGED,
    PLAN_CREATED,
    COMMAND_EXECUTED,
    WAITING_APPROVAL,
    COMPLETED,
    ERROR
}
