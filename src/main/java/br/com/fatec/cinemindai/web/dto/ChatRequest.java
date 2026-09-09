package br.com.fatec.cinemindai.web.dto;

import br.com.fatec.cinemindai.agent.strategy.PlannerMode;

public record ChatRequest(String conversationId, String message, PlannerMode plannerMode) {
}
