package com.vertyll.snaptale.common;

import java.io.Serial;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConflictException(String messageKey) {
        this(messageKey, Map.of());
    }

    public ConflictException(String messageKey, Map<String, Object> messageArgs) {
        super(messageKey, messageArgs);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
