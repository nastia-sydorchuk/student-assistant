package io.reflectoring.demo.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ui_config")
public record PortalCard(
        @Id String id,
        String title,
        String route,
        String icon,
        String description,
        String cardId,
        String language
) {}
