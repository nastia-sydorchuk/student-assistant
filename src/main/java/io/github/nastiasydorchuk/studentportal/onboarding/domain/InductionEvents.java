package io.github.nastiasydorchuk.studentportal.onboarding.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "induction_events")
public record InductionEvents(
        @Id String id,             // e.g., "events_es"
        String language,
        List<InductionEvent> events,
        @JsonIgnore String sourceHash // hash of the English document this was translated from; null on English
) {}
