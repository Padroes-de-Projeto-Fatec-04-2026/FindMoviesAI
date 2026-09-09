package br.com.fatec.cinemindai.agent.command;

import java.time.Instant;
import java.util.Map;

public record CommandExecutionRecord(
        String commandName,
        Map<String, Object> arguments,
        CommandResult result,
        Instant executedAt) {
}
