package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.translation.TranslationCache;
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
    private final TranslationCache translations;

    public ResourceService(ResourceRepository repository, TranslationService translationService, TranslationCache translations) {
        this.repository = repository;
        this.translationService = translationService;
        this.translations = translations;
    }

    public ResourceCategoryResponse getCategory(String categoryId, String language) {
        ResourceCategory english = repository.findByCategoryIdAndLanguage(categoryId, "en")
                .orElseThrow(() -> new RecordNotFoundException("Resource category not found: " + categoryId));

        ResourceCategory result = translations.localize(language, english, ResourceCategoryResponse.from(english),
                lang -> repository.findByCategoryIdAndLanguage(categoryId, lang), ResourceCategory::sourceHash,
                // Same id as a stale copy, so saving overwrites it
                (en, hash) -> repository.save(translationService.translateCategory(en, language, hash)));
        return ResourceCategoryResponse.from(result);
    }

    public List<String> listCategoryIds() {
        // English documents are the source of truth for which categories exist
        return repository.findByLanguage("en").stream()
                .map(ResourceCategory::categoryId)
                .toList();
    }
}
