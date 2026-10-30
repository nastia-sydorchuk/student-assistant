package io.github.nastiasydorchuk.studentportal.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

// The app.* settings in application.properties; comma-separated values bind to lists.
// Validated on startup, so a missing setting stops the app with a message naming it.
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotEmpty List<String> supportedLanguages,
        @NotNull @Valid Rag rag,
        @NotNull @Valid Cors cors
) {
    public record Rag(@NotBlank String url, String apiKey) {} // apiKey is optional

    public record Cors(@NotEmpty List<String> allowedOrigins) {}
}
