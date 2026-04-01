package io.reflectoring.demo.chat;

import io.reflectoring.demo.dto.ChatRequest;
import io.reflectoring.demo.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiService {
    private final WebClient webClient;
    private final String PYTHON_SERVICE_URL = "http://localhost:8000/rag-query";
    @Autowired
    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(PYTHON_SERVICE_URL).build();
    }

    public ChatResponse callGemini(ChatRequest request) {
        try {
            Map<String, String> perfectJsonPayload = new HashMap<>();
            perfectJsonPayload.put("query", request.getQuery());
            perfectJsonPayload.put("session_id", request.getSessionId());
            perfectJsonPayload.put("current_url", request.getCurrentUrl());
            perfectJsonPayload.put("bot_greeting", request.getBotGreeting());

            ChatResponse response = webClient.post()
                    .bodyValue(perfectJsonPayload)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            System.err.println("PYTHON REJECTED THE JSON. Reason: " + e.getResponseBodyAsString());
            return new ChatResponse("I'm sorry, my AI brain is currently offline.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("I'm sorry, my AI brain is currently offline.", null);
        }
    }
}
