package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.resources.api.ResourceCategoryResponse;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository repository;
    private final TranslationService translationService;
    private final SourceHasher hasher;

    public ResourceService(ResourceRepository repository, TranslationService translationService, SourceHasher hasher) {
        this.repository = repository;
        this.translationService = translationService;
        this.hasher = hasher;
    }

    // Translates when the translation is missing or was made from an older version of the English.
    // Returns the language to serve: English if a translation is needed but translation is disabled.
    private String languageToServe(String categoryId, String language) {
        if ("en".equals(language)) return language;

        ResourceCategory english = repository.findByCategoryIdAndLanguage(categoryId, "en")
                .orElseThrow(() -> new RecordNotFoundException("Resource category not found: " + categoryId));
        // Hash the client-visible content only, so storage fields never invalidate translations
        String hash = hasher.hash(ResourceCategoryResponse.from(english));

        boolean upToDate = repository.findByCategoryIdAndLanguage(categoryId, language)
                .map(existing -> hash.equals(existing.getSourceHash()))
                .orElse(false);

        if (!upToDate) {
            if (!translationService.isEnabled()) return "en";
            ResourceCategory translated = translationService.translateCategory(english, language);
            translated.setSourceHash(hash);
            repository.save(translated); // same id as the stale copy, so it overwrites it
        }
        return language;
    }

    public ResourceCategory getCategory(String categoryId, String language) {
        return repository.findByCategoryIdAndLanguage(categoryId, languageToServe(categoryId, language))
                .orElseThrow(() -> new RecordNotFoundException("Resource category not found: " + categoryId));
    }

    public List<String> listCategoryIds() {
        // English documents are the source of truth for which categories exist
        return repository.findByLanguage("en").stream()
                .map(ResourceCategory::getCategoryId)
                .toList();
    }
}
