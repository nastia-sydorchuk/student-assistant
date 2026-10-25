package io.github.nastiasydorchuk.studentportal.portal.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "portal_cards")
public record PortalCard(
        @Id String id,
        String title,
        String route,
        String icon,
        String description,
        String cardId,
        String language,
        String sourceHash // hash of the English cards this was translated from; null on English
) {}
