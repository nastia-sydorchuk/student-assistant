package io.github.nastiasydorchuk.studentportal.chat;

import io.github.nastiasydorchuk.studentportal.chat.api.ChatRequest;
import io.github.nastiasydorchuk.studentportal.chat.api.ChatResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void forwardsTheRequestAndReturnsTheAnswer() throws Exception {
        when(chatService.chat(any())).thenReturn(new ChatResponse("In September.", List.of("https://hw.ac.uk/enrol")));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "When is enrolment?", "session_id": "s1",
                                 "current_url": "/onboarding", "bot_greeting": "Hi!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("In September."))
                .andExpect(jsonPath("$.sources[0]").value("https://hw.ac.uk/enrol"));

        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatService).chat(captor.capture());
        assertThat(captor.getValue().getQuery()).isEqualTo("When is enrolment?");
        assertThat(captor.getValue().getSessionId()).isEqualTo("s1");
        assertThat(captor.getValue().getCurrentUrl()).isEqualTo("/onboarding");
        assertThat(captor.getValue().getBotGreeting()).isEqualTo("Hi!");
    }

    @Test
    void omitsNullSources() throws Exception {
        when(chatService.chat(any())).thenReturn(new ChatResponse("Offline", null));

        mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"query\": \"hi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sources").doesNotExist());
    }

    @Test
    void blankQueryIsRejected() throws Exception {
        mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"query\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.query").exists());

        verifyNoInteractions(chatService);
    }

    @Test
    void missingQueryIsRejected() throws Exception {
        mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.query").exists());
    }

    @Test
    void overlongQueryIsRejected() throws Exception {
        String query = "a".repeat(2001);

        mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"query\": \"" + query + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.query").exists());

        verifyNoInteractions(chatService);
    }

    @Test
    void malformedJsonIsABadRequest() throws Exception {
        mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"query\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.path").value("/api/chat"));

        verifyNoInteractions(chatService);
    }

    @Test
    void missingBodyIsABadRequest() throws Exception {
        mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void nonJsonContentTypeIsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/chat").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));

        verifyNoInteractions(chatService);
    }

    @Test
    void queryAtTheLimitIsAccepted() throws Exception {
        when(chatService.chat(any())).thenReturn(new ChatResponse("ok", null));

        mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"" + "a".repeat(2000) + "\"}"))
                .andExpect(status().isOk());
    }
}
