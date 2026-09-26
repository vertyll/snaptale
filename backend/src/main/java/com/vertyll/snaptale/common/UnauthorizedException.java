package com.vertyll.snaptale.common;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public final class UnauthorizedException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNAUTHORIZED;
    }
}
