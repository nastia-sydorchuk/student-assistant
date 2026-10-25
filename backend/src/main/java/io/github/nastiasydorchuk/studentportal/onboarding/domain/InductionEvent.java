package io.github.nastiasydorchuk.studentportal.onboarding.domain;

public record InductionEvent(
        String title,
        String icon,
        String desc,
        String actionText,
        String url
) {}
