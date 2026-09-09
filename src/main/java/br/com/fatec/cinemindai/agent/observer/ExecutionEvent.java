package br.com.fatec.cinemindai.agent.observer;

import java.time.Instant;

public record ExecutionEvent(String conversationId, ExecutionEventType type, String message, Instant timestamp) {
}
