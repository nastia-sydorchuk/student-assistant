package io.github.nastiasydorchuk.studentportal.onboarding.domain;

public record ImportantDate(
        String activity,
        String date,
        DateType type
) {}
