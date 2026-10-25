package io.github.nastiasydorchuk.studentportal.translation;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// Fingerprints English source content so translations can tell when they are out of date
@Component
public class SourceHasher {
    private final JsonMapper jsonMapper;

    public SourceHasher(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String hash(Object source) {
        try {
            byte[] json = jsonMapper.writeValueAsBytes(source);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
