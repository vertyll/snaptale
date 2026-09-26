package com.vertyll.snaptale.common;

import java.io.Serial;
import java.util.Map;

import org.springframework.http.HttpStatus;

public final class ForbiddenException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String messageKey) {
        super(messageKey, Map.of());
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }
}
