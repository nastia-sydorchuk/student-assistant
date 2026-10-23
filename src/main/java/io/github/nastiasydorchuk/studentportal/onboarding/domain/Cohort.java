package io.github.nastiasydorchuk.studentportal.onboarding.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

// One document per level per language; the API returns it as a CohortResponse
@Document(collection = "cohorts")
public record Cohort(
        @Id String id,             // e.g., "undergraduate_es"
        CohortLevel level,
        String language,
        String title,
        List<ImportantDate> importantDates,
        InductionProgramme event,
        String sourceHash // hash of the English document this was translated from; null on English
) {}
