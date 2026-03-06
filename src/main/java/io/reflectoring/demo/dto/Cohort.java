package io.reflectoring.demo.dto;

import java.util.List;

public record Cohort(
        String level,
        String title,
        List<ImportantDate> importantDates,
        EventDetails event
) {}
