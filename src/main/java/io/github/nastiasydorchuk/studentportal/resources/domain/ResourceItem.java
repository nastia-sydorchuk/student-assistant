package io.github.nastiasydorchuk.studentportal.resources.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceItem(
        String title,
        String icon,
        String description,
        String actionText,
        String url,
        // Optional fields that only some items use
        String label,
        String badgeClass,
        String location,
        String contact
) {}
