package com.vertyll.snaptale.common;

import java.io.Serial;
import java.util.Map;

import org.springframework.http.HttpStatus;

public final class InvalidRequestException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String messageKey) {
        this(messageKey, Map.of());
    }

    public InvalidRequestException(String messageKey, Map<String, Object> messageArgs) {
        super(messageKey, messageArgs);
    }

    public InvalidRequestException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }
}
