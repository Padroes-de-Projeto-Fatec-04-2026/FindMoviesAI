package br.com.fatec.cinemindai.agent.command;

import java.util.Map;

public record CommandContext(Map<String, Object> arguments) {

    public CommandContext {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }

    public String argument(String key) {
        Object value = arguments.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
