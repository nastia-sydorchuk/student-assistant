package io.github.nastiasydorchuk.studentportal.chat.api;

import java.util.List;

public record ChatResponse(
        String answer,
        List<String> sources
) {}
