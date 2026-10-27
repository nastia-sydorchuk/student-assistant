package io.github.nastiasydorchuk.studentportal.chat.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ChatRequest {

    @NotBlank
    @Size(max = 2000)
    private String query;

    // Typed as UUID so a malformed id is rejected here with a 400, not by the RAG service downstream
    @NotNull
    @JsonProperty("session_id")
    private UUID sessionId;

    @JsonProperty("current_url")
    private String currentUrl;

    @JsonProperty("bot_greeting")
    private String botGreeting;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getCurrentUrl() { return currentUrl; }
    public void setCurrentUrl(String currentUrl) { this.currentUrl = currentUrl; }

    public String getBotGreeting() { return botGreeting; }
    public void setBotGreeting(String botGreeting) { this.botGreeting = botGreeting; }
}
