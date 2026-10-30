package io.github.nastiasydorchuk.studentportal.chat;

import io.github.nastiasydorchuk.studentportal.chat.api.ChatRequest;
import io.github.nastiasydorchuk.studentportal.chat.api.ChatResponse;
import io.github.nastiasydorchuk.studentportal.common.error.UpstreamUnavailableException;
import io.github.nastiasydorchuk.studentportal.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ChatServiceTest {

    private static final String RAG_URL = "http://rag.test/rag-query";
    private static final UUID SESSION_ID = UUID.fromString("7f3c2a9e-4b1d-4e8a-9c6f-2d5b8e1a0f43");

    private MockRestServiceServer server;

    private ChatService service(String apiKey) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        return new ChatService(builder, new AppProperties(null, new AppProperties.Rag(RAG_URL, apiKey), null));
    }

    private static ChatRequest request() {
        return new ChatRequest("When is enrolment?", SESSION_ID, "Hi!");
    }

    @Test
    void forwardsTheQueryAsSnakeCaseJsonAndReturnsTheAnswer() {
        ChatService service = service("");
        server.expect(requestTo(RAG_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"query": "When is enrolment?", "session_id": "7f3c2a9e-4b1d-4e8a-9c6f-2d5b8e1a0f43",
                         "bot_greeting": "Hi!"}
                        """, true))
                .andExpect(headerDoesNotExist("X-API-Key"))
                .andRespond(withSuccess("""
                        {"answer": "In September.", "sources": ["https://hw.ac.uk/enrol"]}
                        """, MediaType.APPLICATION_JSON));

        ChatResponse response = service.chat(request());

        assertThat(response).isEqualTo(new ChatResponse("In September.", List.of("https://hw.ac.uk/enrol")));
        server.verify();
    }

    @Test
    void sendsTheApiKeyWhenConfigured() {
        ChatService service = service("secret");
        server.expect(requestTo(RAG_URL))
                .andExpect(header("X-API-Key", "secret"))
                .andRespond(withSuccess("{\"answer\": \"ok\"}", MediaType.APPLICATION_JSON));

        service.chat(request());
        server.verify();
    }

    @Test
    void throwsWhenTheRagServiceReturnsAnError() {
        ChatService service = service("");
        server.expect(requestTo(RAG_URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("boom"));

        assertThatThrownBy(() -> service.chat(request()))
                .isInstanceOf(UpstreamUnavailableException.class)
                .hasMessage("The chat assistant is temporarily unavailable.");
    }

    @Test
    void throwsWhenTheRagServiceRejectsTheRequest() {
        ChatService service = service("");
        server.expect(requestTo(RAG_URL)).andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT));

        assertThatThrownBy(() -> service.chat(request())).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void throwsWhenTheRagServiceIsUnreachable() {
        ChatService service = service("");
        server.expect(requestTo(RAG_URL)).andRespond(withException(new IOException("connection refused")));

        assertThatThrownBy(() -> service.chat(request())).isInstanceOf(UpstreamUnavailableException.class);
    }
}
