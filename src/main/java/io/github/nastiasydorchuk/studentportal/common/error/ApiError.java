package io.github.nastiasydorchuk.studentportal.common.error;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        int status,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> errors
) {
    public static ApiError of(int status, String message, String path) {
        return new ApiError(status, message, path, Instant.now(), null);
    }

    public static ApiError of(int status, String message, String path, Map<String, String> errors) {
        return new ApiError(status, message, path, Instant.now(), errors);
    }
}
