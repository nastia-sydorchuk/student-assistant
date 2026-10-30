package io.github.nastiasydorchuk.studentportal.chat;

import io.github.nastiasydorchuk.studentportal.chat.api.ChatRequest;
import io.github.nastiasydorchuk.studentportal.chat.api.ChatResponse;
import io.github.nastiasydorchuk.studentportal.common.error.UpstreamUnavailableException;
import io.github.nastiasydorchuk.studentportal.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String UNAVAILABLE_MESSAGE = "The chat assistant is temporarily unavailable.";

    private final RestClient restClient;

    public ChatService(RestClient.Builder restClientBuilder, AppProperties properties) {
        AppProperties.Rag rag = properties.rag();
        // Clone: the builder is a shared bean, and the API key must not leak into other clients
        RestClient.Builder builder = restClientBuilder.clone().baseUrl(rag.url());
        if (StringUtils.hasText(rag.apiKey())) {
            builder.defaultHeader("X-API-Key", rag.apiKey());
        }
        this.restClient = builder.build();
    }

    public ChatResponse chat(ChatRequest request) {
        try {
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
