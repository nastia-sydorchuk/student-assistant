package io.reflectoring.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatRequest {

    private String query;

    @JsonProperty("session_id")
    private String sessionId;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCurrentUrl() { return currentUrl; }
    public void setCurrentUrl(String currentUrl) { this.currentUrl = currentUrl; }

    public String getBotGreeting() { return botGreeting; }
    public void setBotGreeting(String botGreeting) { this.botGreeting = botGreeting; }
}
