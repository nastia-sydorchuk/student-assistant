package io.reflectoring.demo.chat;

import io.reflectoring.demo.dto.ChatRequest;
import io.reflectoring.demo.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiService {
    private final RestClient restClient;

    @Autowired
    public GeminiService(RestClient.Builder restClientBuilder, @Value("${app.rag.url}") String ragServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(ragServiceUrl).build();
    }

    public ChatResponse callGemini(ChatRequest request) {
        try {
            Map<String, String> perfectJsonPayload = new HashMap<>();
            perfectJsonPayload.put("query", request.getQuery());
            perfectJsonPayload.put("session_id", request.getSessionId());
            perfectJsonPayload.put("current_url", request.getCurrentUrl());
            perfectJsonPayload.put("bot_greeting", request.getBotGreeting());

            ChatResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(perfectJsonPayload)
                    .retrieve()
                    .body(ChatResponse.class);

            return response;

        } catch (RestClientResponseException e) {
            System.err.println("PYTHON REJECTED THE JSON. Reason: " + e.getResponseBodyAsString());
            return new ChatResponse("I'm sorry, my AI brain is currently offline.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("I'm sorry, my AI brain is currently offline.", null);
        }
    }
}
