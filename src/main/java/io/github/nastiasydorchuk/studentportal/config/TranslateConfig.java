package io.github.nastiasydorchuk.studentportal.config;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import io.github.nastiasydorchuk.studentportal.translation.TranslationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TranslationProperties.class)
public class TranslateConfig {

    @Bean
    public Translate translate(TranslationProperties properties) {
        return TranslateOptions.newBuilder()
                .setApiKey(properties.apiKey())
                .build()
                .getService();
    }
}
