package com.vertyll.snaptale.common;

import java.io.Serial;
import java.util.Map;

import org.springframework.http.HttpStatus;

public final class TooManyRequestsException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TooManyRequestsException(String messageKey, Map<String, Object> messageArgs) {
        super(messageKey, messageArgs);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}
