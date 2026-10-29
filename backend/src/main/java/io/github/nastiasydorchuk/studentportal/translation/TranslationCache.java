package io.github.nastiasydorchuk.studentportal.translation;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

// The one rule every translated section follows. Content is written in English; a translation is
// stored with a hash of the English it came from and redone only when that English changes.
// With translation disabled (no API key), a missing or outdated translation is served in English.
@Component
public class TranslationCache {
    private final TranslationService translationService;
    private final SourceHasher hasher;

    public TranslationCache(TranslationService translationService, SourceHasher hasher) {
        this.translationService = translationService;
        this.hasher = hasher;
    }

    // Only visibleContent (what clients see) is hashed, so storage fields like id and language never
    // invalidate a translation. translateAndSave gets the English document and the hash to stamp it with.
    public <T> T localize(String language, T english, Object visibleContent,
                          Function<String, Optional<T>> findStored,
                          Function<T, String> storedHash,
                          BiFunction<T, String, T> translateAndSave) {
        if ("en".equals(language)) return english;

        String hash = hasher.hash(visibleContent);
        return findStored.apply(language)
                .filter(stored -> hash.equals(storedHash.apply(stored)))
                .orElseGet(() -> translationService.isEnabled() ? translateAndSave.apply(english, hash) : english);
    }
}
