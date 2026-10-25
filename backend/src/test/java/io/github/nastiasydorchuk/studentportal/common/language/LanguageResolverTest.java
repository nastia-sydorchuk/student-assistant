package io.github.nastiasydorchuk.studentportal.common.language;

import io.github.nastiasydorchuk.studentportal.common.error.UnsupportedLanguageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LanguageResolverTest {

    private final LanguageResolver resolver = new LanguageResolver("en,es,fr,de");

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "en|en",
            "ES|es",
            "fr-CA|fr",
            "de-DE-1996|de",
            "' es '|es",
            "en-GB,en;q=0.9,fr;q=0.8|en",
            "fr;q=0.9|fr"
    })
    void resolvesSupportedLanguagesToTheirPrimarySubtag(String header, String expected) {
        assertThat(resolver.resolve(header)).isEqualTo(expected);
    }

    @Test
    void rejectsLanguageOutsideTheAllowlist() {
        assertThatThrownBy(() -> resolver.resolve("uk-UA"))
                .isInstanceOf(UnsupportedLanguageException.class)
                .hasMessage("Unsupported language: uk-UA");
    }

    @Test
    void rejectsEmptyHeader() {
        assertThatThrownBy(() -> resolver.resolve(""))
                .isInstanceOf(UnsupportedLanguageException.class);
    }

    @Test
    void onlyThePrimaryTagIsConsidered() {
        // "uk" comes first, so the later supported "en" must not be picked
        assertThatThrownBy(() -> resolver.resolve("uk,en;q=0.5"))
                .isInstanceOf(UnsupportedLanguageException.class);
    }

    @Test
    void normalizesTheConfiguredList() {
        LanguageResolver messy = new LanguageResolver(" en , ES ,,fr ");

        assertThat(messy.getSupportedLanguages()).containsExactly("en", "es", "fr");
    }
}
