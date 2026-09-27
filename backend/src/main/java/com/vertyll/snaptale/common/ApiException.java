package com.vertyll.snaptale.common;

import java.io.Serial;
import java.util.Map;

import org.springframework.http.HttpStatus;

abstract class ApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String messageKey;
    private final transient Map<String, Object> messageArgs;

    protected ApiException(String messageKey, Map<String, Object> messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = Map.copyOf(messageArgs);
    }

    protected ApiException(String messageKey, Throwable cause) {
        super(messageKey, cause);
        this.messageKey = messageKey;
        this.messageArgs = Map.of();
    }

    public String messageKey() {
        return messageKey;
    }

    public Map<String, Object> messageArgs() {
        return messageArgs;
    }

    public abstract HttpStatus status();
}
