package io.reflectoring.demo.pages;

import io.reflectoring.demo.TranslationService;
import io.reflectoring.demo.common.language.LanguageResolver;
import io.reflectoring.demo.dto.ResourceCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceRepository repository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private LanguageResolver languageResolver;

    @GetMapping("/{categoryId}")
    public ResponseEntity<ResourceCategory> getCategory(
            @PathVariable String categoryId,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {

        String resolvedLanguage = languageResolver.resolve(language);

        Optional<ResourceCategory> cachedCategory = repository.findByCategoryIdAndLanguage(categoryId, resolvedLanguage);

        if (cachedCategory.isPresent()) {
            return ResponseEntity.ok(cachedCategory.get());
        }

        Optional<ResourceCategory> englishCategory = repository.findByCategoryIdAndLanguage(categoryId, "en");

        if (englishCategory.isEmpty()) {
            return ResponseEntity.notFound().build(); // Category doesn't exist at all
        }

        ResourceCategory newlyTranslatedCategory = translationService.translateCategory(englishCategory.get(), resolvedLanguage);

        repository.save(newlyTranslatedCategory);

        return ResponseEntity.ok(newlyTranslatedCategory);
    }
//    @GetMapping("/{id}")
//    public ResponseEntity<ResourceCategory> getCategoryById(@PathVariable String id) {
//        Optional<ResourceCategory> category = repository.findById(id);
//
//        return category.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
}