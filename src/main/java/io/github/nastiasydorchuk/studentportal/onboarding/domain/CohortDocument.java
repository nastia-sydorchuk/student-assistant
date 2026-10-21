package io.github.nastiasydorchuk.studentportal.onboarding.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

// One document per level per language; the API returns it as a Cohort
@Document(collection = "cohorts")
public record CohortDocument(
        @Id String id,             // e.g., "undergraduate_es"
        String level,              // e.g., "undergraduate"
        String language,
        String title,
        List<ImportantDate> importantDates,
        EventDetails event,
        @JsonIgnore String sourceHash // hash of the English document this was translated from; null on English
) {
    public Cohort toCohort() {
        return new Cohort(level, title, importantDates, event);
    }
}
