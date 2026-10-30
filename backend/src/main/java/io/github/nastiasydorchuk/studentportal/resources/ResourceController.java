package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.resources.api.ResourceCategoryResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource-categories")
public class ResourceController {

    private final ResourceService service;
    private final LanguageResolver languageResolver;

    public ResourceController(ResourceService service, LanguageResolver languageResolver) {
        this.service = service;
        this.languageResolver = languageResolver;
    }

    @GetMapping
    public List<String> listCategoryIds() {
        return service.listCategoryIds();
    }

    @GetMapping("/{categoryId}")
    public ResourceCategoryResponse getCategory(@PathVariable String categoryId, @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return service.getCategory(categoryId, languageResolver.resolve(language));
    }
}
