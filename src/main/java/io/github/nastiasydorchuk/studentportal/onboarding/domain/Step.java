package io.github.nastiasydorchuk.studentportal.onboarding.domain;

public record Step(int order, String summary, String description, String actionText, String url) { }

