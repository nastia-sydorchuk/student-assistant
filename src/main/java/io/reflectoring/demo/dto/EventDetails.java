package io.reflectoring.demo.dto;

import java.util.List;

public record EventDetails(
        String title,
        String description,
        List<String> programmeOutline, // The bullet points

        // Eligibility: PG uses this string
        String eligibility,

        // Eligibility: UG uses this list
        List<String> eligibilityList,

        // Contact info for UG
        String contactInfo,

        RegistrationLink registration
) {}

