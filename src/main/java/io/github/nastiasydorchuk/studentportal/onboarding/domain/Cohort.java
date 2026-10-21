package io.github.nastiasydorchuk.studentportal.onboarding.domain;

import java.util.List;

public record Cohort(
        CohortLevel level,
        String title,
        List<ImportantDate> importantDates,
        EventDetails event
) {}
