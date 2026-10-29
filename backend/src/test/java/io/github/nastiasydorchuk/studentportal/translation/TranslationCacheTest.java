package io.github.nastiasydorchuk.studentportal.translation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TranslationCacheTest {

    record Doc(String language, String text, String sourceHash) {}

    private final Doc english = new Doc("en", "Hello", null);

    private TranslationService translationService;
    private TranslationCache cache;
    private String englishHash;
    // A fake repository: stored translations by language, and the languages looked up and translated
    private final Map<String, Doc> stored = new HashMap<>();
    private final List<String> lookedUp = new ArrayList<>();
    private final List<String> translated = new ArrayList<>();

    @BeforeEach
    void setUp() {
        translationService = mock(TranslationService.class);
        when(translationService.isEnabled()).thenReturn(true);
        SourceHasher hasher = new SourceHasher(JsonMapper.builder().build());
        cache = new TranslationCache(translationService, hasher);
        englishHash = hasher.hash(english.text());
    }

    private Doc localize(String language) {
        return cache.localize(language, english, english.text(),
                lang -> {
                    lookedUp.add(lang);
                    return Optional.ofNullable(stored.get(lang));
                },
                Doc::sourceHash,
                (en, hash) -> {
                    translated.add(language);
                    Doc translation = new Doc(language, language + ":" + en.text(), hash);
                    stored.put(language, translation);
                    return translation;
                });
    }

    @Test
    void returnsEnglishWithoutLookingAnythingUp() {
        assertThat(localize("en")).isSameAs(english);
        assertThat(lookedUp).isEmpty();
        assertThat(translated).isEmpty();
    }

    @Test
    void returnsStoredTranslationMadeFromTheCurrentEnglish() {
        Doc spanish = new Doc("es", "Hola", englishHash);
        stored.put("es", spanish);

        assertThat(localize("es")).isSameAs(spanish);
        assertThat(translated).isEmpty();
    }

    @Test
    void translatesAndSavesWhenNothingIsStored() {
        assertThat(localize("es")).isEqualTo(new Doc("es", "es:Hello", englishHash));
        assertThat(stored.get("es")).isEqualTo(new Doc("es", "es:Hello", englishHash));
    }

    @Test
    void retranslatesWhenTheStoredTranslationIsOutdated() {
        stored.put("es", new Doc("es", "Hola", "old-hash"));

        assertThat(localize("es")).isEqualTo(new Doc("es", "es:Hello", englishHash));
        assertThat(translated).containsExactly("es");
    }

    @Test
    void servesEnglishWhenTranslationIsDisabled() {
        when(translationService.isEnabled()).thenReturn(false);

        assertThat(localize("es")).isSameAs(english);
        assertThat(translated).isEmpty();
    }

    @Test
    void servesEnglishRatherThanAnOutdatedTranslationWhenTranslationIsDisabled() {
        when(translationService.isEnabled()).thenReturn(false);
        stored.put("es", new Doc("es", "Hola", "old-hash"));

        assertThat(localize("es")).isSameAs(english);
        assertThat(translated).isEmpty();
    }
}
