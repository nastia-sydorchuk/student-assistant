package io.github.nastiasydorchuk.studentportal.portal.domain;

public record PortalCard(
        String cardId,
        String title,
        String route,
        String icon,
        String description
) {}
