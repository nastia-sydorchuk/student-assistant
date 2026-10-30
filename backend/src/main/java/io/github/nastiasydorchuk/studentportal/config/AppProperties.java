package io.github.nastiasydorchuk.studentportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// The app.* settings in application.properties; comma-separated values bind to lists
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        List<String> supportedLanguages,
        Rag rag,
        Cors cors
) {
    public record Rag(String url, String apiKey) {}

    public record Cors(List<String> allowedOrigins) {}
}
