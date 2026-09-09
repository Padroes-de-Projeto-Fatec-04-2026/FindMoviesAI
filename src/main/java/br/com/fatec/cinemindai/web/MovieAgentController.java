package br.com.fatec.cinemindai.web;

import br.com.fatec.cinemindai.agent.core.AgentInteractionResult;
import br.com.fatec.cinemindai.agent.core.MovieRecommendationAgentService;
import br.com.fatec.cinemindai.agent.observer.SseAgentEventListener;
import br.com.fatec.cinemindai.web.dto.ChatRequest;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/agent")
public class MovieAgentController {

    private final MovieRecommendationAgentService agentService;
    private final SseAgentEventListener sseAgentEventListener;

    public MovieAgentController(MovieRecommendationAgentService agentService, SseAgentEventListener sseAgentEventListener) {
        this.agentService = agentService;
        this.sseAgentEventListener = sseAgentEventListener;
    }

    @PostMapping("/chat")
    public AgentInteractionResult chat(@RequestBody ChatRequest request) {
        String conversationId = request.conversationId() != null && !request.conversationId().isBlank()
                ? request.conversationId()
                : UUID.randomUUID().toString();
        return agentService.handleMessage(conversationId, request.message(), request.plannerMode());
    }

    @PostMapping("/chat/{conversationId}/approve")
    public AgentInteractionResult approve(@PathVariable String conversationId) {
        return agentService.approve(conversationId);
    }

    @GetMapping(path = "/chat/{conversationId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable String conversationId) {
        return sseAgentEventListener.subscribe(conversationId);
    }
}
