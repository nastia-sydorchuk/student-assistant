package io.github.nastiasydorchuk.studentportal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AppPropertiesTest {

    @EnableConfigurationProperties(AppProperties.class)
    static class Config {}

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues(
                    "app.supported-languages=en,es",
                    "app.rag.url=http://rag:8000/rag-query",
                    "app.cors.allowed-origins=http://localhost:4200");

    @Test
    void bindsTheSettings() {
        runner.run(context -> {
            AppProperties properties = context.getBean(AppProperties.class);

            assertThat(properties.supportedLanguages()).containsExactly("en", "es");
            assertThat(properties.rag().url()).isEqualTo("http://rag:8000/rag-query");
            assertThat(properties.rag().apiKey()).isNull();
            assertThat(properties.cors().allowedOrigins()).containsExactly("http://localhost:4200");
        });
    }

    @Test
    void refusesToStartWithoutARagUrl() {
        runner.withPropertyValues("app.rag.url=").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("rag.url");
        });
    }

    @Test
    void refusesToStartWithoutSupportedLanguages() {
        runner.withPropertyValues("app.supported-languages=").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("supportedLanguages");
        });
    }
}
