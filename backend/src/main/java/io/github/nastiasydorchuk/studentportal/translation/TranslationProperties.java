package io.github.nastiasydorchuk.studentportal.translation;

import org.springframework.boot.context.properties.ConfigurationProperties;

// apiKey is optional: without it, translation is disabled (see TranslateConfig)
@ConfigurationProperties(prefix = "google.translate")
public record TranslationProperties(
        String apiKey
) {
}
