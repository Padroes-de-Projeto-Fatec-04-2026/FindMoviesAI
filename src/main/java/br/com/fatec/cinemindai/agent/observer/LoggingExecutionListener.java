package br.com.fatec.cinemindai.agent.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingExecutionListener implements AgentExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingExecutionListener.class);

    @Override
    public void onEvent(ExecutionEvent event) {
        log.info("[{}] {} - {}", event.conversationId(), event.type(), event.message());
    }
}
