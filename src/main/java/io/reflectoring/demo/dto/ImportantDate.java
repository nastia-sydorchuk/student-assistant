package io.reflectoring.demo.dto;

public record ImportantDate(
        String activity,
        String date,
        String type // "action", "deadline", "event"
) {}
