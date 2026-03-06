package io.reflectoring.demo.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "welcome")
public record WelcomeDTO(
        @Id String id,
        String source,
        List<StepDTO> steps,
        List<Cohort> cohorts,
        List<InductionEvent> inductionEvents
) {}
