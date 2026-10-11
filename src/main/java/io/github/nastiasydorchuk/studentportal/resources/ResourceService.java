package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {
    private final ResourceRepository repository;
    private final TranslationService translationService;

    public ResourceService(ResourceRepository repository, TranslationService translationService) {
        this.repository = repository;
        this.translationService = translationService;
    }

    private void ensureTranslationExists(String categoryId, String language) {
        if (repository.findByCategoryIdAndLanguage(categoryId, language).isEmpty()) {
            ResourceCategory english = repository.findByCategoryIdAndLanguage(categoryId, "en")
                    .orElseThrow(() -> new RecordNotFoundException("Resource category not found: " + categoryId));

            ResourceCategory translated = translationService.translateCategory(english, language);
            repository.save(translated);
        }
    }

    public ResourceCategory getCategory(String categoryId, String language) {
        ensureTranslationExists(categoryId, language);
        return repository.findByCategoryIdAndLanguage(categoryId, language)
                .orElseThrow(() -> new RecordNotFoundException("Resource category not found: " + categoryId));
    }
}
