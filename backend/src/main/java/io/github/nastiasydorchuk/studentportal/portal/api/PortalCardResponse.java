package io.github.nastiasydorchuk.studentportal.portal.api;

import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;

// What clients see of a portal card; storage keys like id, language and sourceHash stay internal
public record PortalCardResponse(
        String cardId,
        String title,
        String route,
        String icon,
        String description
) {
    public static PortalCardResponse from(PortalCard card) {
        return new PortalCardResponse(card.cardId(), card.title(), card.route(), card.icon(), card.description());
    }
}
