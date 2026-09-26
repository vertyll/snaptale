package com.vertyll.snaptale.common;

import java.io.Serial;
import java.util.Map;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String messageKey) {
        this(messageKey, Map.of());
    }

    public NotFoundException(String messageKey, Map<String, Object> messageArgs) {
        super(messageKey, messageArgs);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
