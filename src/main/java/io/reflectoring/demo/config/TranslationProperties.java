package io.reflectoring.demo.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "google.translate")
public record TranslationProperties(
        @NotBlank String apiKey
) {
}
