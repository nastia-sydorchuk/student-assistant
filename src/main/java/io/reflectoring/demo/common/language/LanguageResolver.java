package io.reflectoring.demo.common.language;

import io.reflectoring.demo.exceptions.UnsupportedLanguageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// Validates a client-supplied language code against the configured, product-decided
// allowlist (app.supported-languages). This is independent of whether a translation
// for that language already exists in Mongo: the DB is just a cache, this is the gate
// that decides which language codes are allowed to trigger a Google Translate call at all.
@Component
public class LanguageResolver {

    private final List<String> supportedLanguages;

    public LanguageResolver(@Value("${app.supported-languages}") String supportedLanguages) {
        this.supportedLanguages = Arrays.stream(supportedLanguages.split(","))
                .map(String::trim)
                .map(code -> code.toLowerCase(Locale.ROOT))
                .filter(code -> !code.isEmpty())
                .toList();
    }

    public String resolve(String acceptLanguageHeader) {
        String code = baseTag(acceptLanguageHeader);
        if (!supportedLanguages.contains(code)) {
            throw new UnsupportedLanguageException("Unsupported language: " + acceptLanguageHeader);
        }
        return code;
    }

    // Defensively reduces a full Accept-Language value (e.g. "en-GB,en;q=0.9,fr;q=0.8")
    // to its primary subtag, in case a caller other than the known frontend sends one.
    private String baseTag(String header) {
        String firstTag = header.split(",")[0].split(";")[0].trim();
        String[] parts = firstTag.split("-");
        return parts[0].toLowerCase(Locale.ROOT);
    }
}
