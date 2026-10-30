package io.github.nastiasydorchuk.studentportal.common.language;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

// Exposes the configured allowlist so the frontend's language switcher doesn't
// have to hardcode its own copy of app.supported-languages. Names are given in the
// language itself ("Українська", not "Ukrainian"), as is conventional for switchers.
@RestController
@RequestMapping("/api/languages")
public class LanguageController {

    private final LanguageResolver languageResolver;

    public LanguageController(LanguageResolver languageResolver) {
        this.languageResolver = languageResolver;
    }

    @GetMapping
    public List<LanguageOption> getSupportedLanguages() {
        return languageResolver.getSupportedLanguages().stream()
                .map(code -> new LanguageOption(code, nativeName(code)))
                .toList();
    }

    // Falls back to the code itself if the JDK has no name for it.
    private String nativeName(String code) {
        Locale locale = Locale.forLanguageTag(code);
        String name = locale.getDisplayLanguage(locale);
        if (name.isEmpty() || name.equalsIgnoreCase(code)) {
            return code;
        }
        // Some languages lowercase their own name ("français"); capitalize for display.
        return name.substring(0, 1).toUpperCase(locale) + name.substring(1);
    }
}
