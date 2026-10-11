package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService service;
    private final LanguageResolver languageResolver;

    public ResourceController(ResourceService service, LanguageResolver languageResolver) {
        this.service = service;
        this.languageResolver = languageResolver;
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ResourceCategory> getCategory(@PathVariable String categoryId, @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getCategory(categoryId, languageResolver.resolve(language)));
    }
}
