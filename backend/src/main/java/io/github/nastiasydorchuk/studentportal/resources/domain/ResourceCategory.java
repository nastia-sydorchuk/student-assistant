package io.github.nastiasydorchuk.studentportal.resources.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "resource_categories")
public record ResourceCategory(
        @Id String id,             // e.g., "academic_es"
        String categoryId,         // e.g., "academic"
        String language,
        String categoryTitle,
        String description,
        List<ResourceItem> items,
        String sourceHash // hash of the English category this was translated from; null on English
) {}
