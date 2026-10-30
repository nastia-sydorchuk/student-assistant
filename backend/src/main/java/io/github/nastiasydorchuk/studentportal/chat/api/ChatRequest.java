package io.github.nastiasydorchuk.studentportal.chat.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Serializes to exactly the RAG service's request schema
public record ChatRequest(
        // Same limit as the RAG service, so an overlong query fails here with a clear 400
        @NotBlank @Size(max = 1000) String query,

        // Typed as UUID so a malformed id is rejected here with a 400, not by the RAG service downstream
        @NotNull @JsonProperty("session_id") UUID sessionId,

        @JsonProperty("bot_greeting") String botGreeting
) {}
