package io.github.nastiasydorchuk.studentportal.onboarding.domain;

import java.util.List;

public record EventDetails(
        String title,
        String description,
        List<String> programmeOutline, // The bullet points

        // Eligibility criteria; PG has a single entry, UG has several
        List<String> eligibility,

        // Contact info for UG
        String contactInfo,

        RegistrationLink registration
) {}

