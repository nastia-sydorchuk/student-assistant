package io.github.nastiasydorchuk.studentportal.onboarding.domain;

public record OnboardingStep(int order, String summary, String description, String actionText, String url) { }

