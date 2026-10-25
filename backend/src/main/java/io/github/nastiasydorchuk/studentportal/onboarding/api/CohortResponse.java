package io.github.nastiasydorchuk.studentportal.onboarding.api;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.Cohort;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.CohortLevel;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.ImportantDate;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.InductionProgramme;

import java.util.List;

// What clients see of a cohort; storage keys like id, language and sourceHash stay internal
public record CohortResponse(
        CohortLevel level,
        String title,
        List<ImportantDate> importantDates,
        InductionProgramme event
) {
    public static CohortResponse from(Cohort cohort) {
        return new CohortResponse(cohort.level(), cohort.title(), cohort.importantDates(), cohort.event());
    }
}
