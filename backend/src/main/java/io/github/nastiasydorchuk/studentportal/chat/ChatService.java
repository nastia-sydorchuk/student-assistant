package io.github.nastiasydorchuk.studentportal.chat;

import io.github.nastiasydorchuk.studentportal.chat.api.ChatRequest;
import io.github.nastiasydorchuk.studentportal.chat.api.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final RestClient restClient;

    public ChatService(RestClient.Builder restClientBuilder, @Value("${app.rag.url}") String ragServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(ragServiceUrl).build();
    }

    public ChatResponse chat(ChatRequest request) {
        try {
            Map<String, String> perfectJsonPayload = new HashMap<>();
            perfectJsonPayload.put("query", request.getQuery());
            perfectJsonPayload.put("session_id", request.getSessionId().toString());
            perfectJsonPayload.put("current_url", request.getCurrentUrl());
            perfectJsonPayload.put("bot_greeting", request.getBotGreeting());

            return restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(perfectJsonPayload)
                    .retrieve()
                    .body(ChatResponse.class);
        } catch (RestClientResponseException e) {
            logger.warn("RAG service rejected the request: {}", e.getStatusCode());
            logger.debug("RAG service response body: {}", e.getResponseBodyAsString());
            return new ChatResponse("I'm sorry, my AI brain is currently offline.", null);

        } catch (Exception e) {
            logger.error("RAG service call failed", e);
            return new ChatResponse("I'm sorry, my AI brain is currently offline.", null);
        }
    }
}
