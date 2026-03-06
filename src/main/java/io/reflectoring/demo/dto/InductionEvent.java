package io.reflectoring.demo.dto;

public record InductionEvent(
        String title,
        String icon,
        String desc,
        String actionText,
        String url
) {}
