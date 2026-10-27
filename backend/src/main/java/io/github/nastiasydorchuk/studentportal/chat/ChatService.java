package io.github.nastiasydorchuk.studentportal.chat;

import io.github.nastiasydorchuk.studentportal.chat.api.ChatRequest;
import io.github.nastiasydorchuk.studentportal.chat.api.ChatResponse;
import io.github.nastiasydorchuk.studentportal.common.error.UpstreamUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String UNAVAILABLE_MESSAGE = "The chat assistant is temporarily unavailable.";

    private final RestClient restClient;

    public ChatService(RestClient.Builder restClientBuilder,
                       @Value("${app.rag.url}") String ragServiceUrl,
                       @Value("${app.rag.api-key:}") String ragApiKey) {
        // Clone: the builder is a shared bean, and the API key must not leak into other clients
        RestClient.Builder builder = restClientBuilder.clone().baseUrl(ragServiceUrl);
        if (!ragApiKey.isBlank()) {
            builder.defaultHeader("X-API-Key", ragApiKey);
        }
        this.restClient = builder.build();
    }

    public ChatResponse chat(ChatRequest request) {
        try {
            // ChatRequest serializes to exactly the RAG service's request schema
            return restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);
        } catch (RestClientResponseException e) {
            logger.debug("RAG service response body: {}", e.getResponseBodyAsString());
            throw new UpstreamUnavailableException(UNAVAILABLE_MESSAGE, e);
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException(UNAVAILABLE_MESSAGE, e);
        }
    }
}
