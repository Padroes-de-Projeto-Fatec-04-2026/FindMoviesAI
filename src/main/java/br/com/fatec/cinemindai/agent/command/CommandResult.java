package br.com.fatec.cinemindai.agent.command;

public record CommandResult(boolean success, String summary, Object data) {
}
