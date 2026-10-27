package io.github.nastiasydorchuk.studentportal.chat;

import io.github.nastiasydorchuk.studentportal.chat.api.ChatRequest;
import io.github.nastiasydorchuk.studentportal.chat.api.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ChatServiceTest {

    private static final String RAG_URL = "http://rag.test/rag-query";
    private static final String OFFLINE_ANSWER = "I'm sorry, my AI brain is currently offline.";
    private static final UUID SESSION_ID = UUID.fromString("7f3c2a9e-4b1d-4e8a-9c6f-2d5b8e1a0f43");

    private MockRestServiceServer server;
    private ChatService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new ChatService(builder, RAG_URL);
    }

    private static ChatRequest request() {
        ChatRequest request = new ChatRequest();
        request.setQuery("When is enrolment?");
        request.setSessionId(SESSION_ID);
        request.setCurrentUrl("/onboarding");
        request.setBotGreeting("Hi!");
        return request;
    }

    @Test
    void forwardsTheQueryAsSnakeCaseJsonAndReturnsTheAnswer() {
        server.expect(requestTo(RAG_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"query": "When is enrolment?", "session_id": "7f3c2a9e-4b1d-4e8a-9c6f-2d5b8e1a0f43",
                         "current_url": "/onboarding", "bot_greeting": "Hi!"}
                        """, true))
                .andRespond(withSuccess("""
                        {"answer": "In September.", "sources": ["https://hw.ac.uk/enrol"]}
                        """, MediaType.APPLICATION_JSON));

        ChatResponse response = service.chat(request());

        assertThat(response.getAnswer()).isEqualTo("In September.");
        assertThat(response.getSources()).containsExactly("https://hw.ac.uk/enrol");
        server.verify();
    }

    @Test
    void fallsBackWhenTheRagServiceReturnsAnError() {
        server.expect(requestTo(RAG_URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("boom"));

        ChatResponse response = service.chat(request());

        assertThat(response.getAnswer()).isEqualTo(OFFLINE_ANSWER);
        assertThat(response.getSources()).isNull();
    }

    @Test
    void fallsBackWhenTheRagServiceRejectsTheRequest() {
        server.expect(requestTo(RAG_URL)).andRespond(withBadRequest());

        assertThat(service.chat(request()).getAnswer()).isEqualTo(OFFLINE_ANSWER);
    }

    @Test
    void fallsBackWhenTheRagServiceIsUnreachable() {
        server.expect(requestTo(RAG_URL)).andRespond(withException(new IOException("connection refused")));

        assertThat(service.chat(request()).getAnswer()).isEqualTo(OFFLINE_ANSWER);
    }
}
