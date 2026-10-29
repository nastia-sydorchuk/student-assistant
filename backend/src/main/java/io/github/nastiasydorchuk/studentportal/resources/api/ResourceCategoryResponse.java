package io.github.nastiasydorchuk.studentportal.resources.api;

import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;

import java.util.List;

// What clients see of a resource category; storage keys like id, language and sourceHash stay internal
public record ResourceCategoryResponse(
        String categoryId,
        String categoryTitle,
        String description,
        List<ResourceItem> items
) {
    public static ResourceCategoryResponse from(ResourceCategory category) {
        return new ResourceCategoryResponse(category.categoryId(), category.categoryTitle(),
                category.description(), category.items());
    }
}
