package br.com.fatec.cinemindai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura o {@link ChatClient} usado por todo o agente, apontando para o modelo Ollama
 * local (auto-configurado pelo starter spring-ai-starter-model-ollama). Os advisors registrados
 * aqui (memória de conversa + logging) são a Chain of Responsibility nativa do Spring AI citada
 * no enunciado do trabalho — não reinventamos esse padrão, apenas o configuramos.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(50).build();
    }

    @Bean
    public ChatClient movieAgentChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor())
                .build();
    }
}
