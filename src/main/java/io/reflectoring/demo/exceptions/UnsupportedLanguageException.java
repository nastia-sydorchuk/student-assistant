package io.reflectoring.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UnsupportedLanguageException extends RuntimeException {

    public UnsupportedLanguageException(String message) {
        super(message);
    }
}
