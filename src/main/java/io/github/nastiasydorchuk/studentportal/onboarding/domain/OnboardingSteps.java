package io.github.nastiasydorchuk.studentportal.onboarding.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "onboarding_steps")
public record OnboardingSteps(
        @Id String id,             // e.g., "steps_es"
        String language,
        List<Step> steps,
        String sourceHash // hash of the English document this was translated from; null on English
) {}
