package io.reflectoring.demo.config;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
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
