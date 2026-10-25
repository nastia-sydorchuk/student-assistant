package io.github.nastiasydorchuk.studentportal.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Thrown when a downstream dependency (Translate, the Python RAG service, hw.ac.uk) fails or times out
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class UpstreamUnavailableException extends RuntimeException {

    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpstreamUnavailableException(String message) {
        super(message);
    }
}
