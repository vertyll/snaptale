package com.vertyll.snaptale.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.TooManyRequestsException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class LoginThrottle {

    private final Map<String, Failures> failures = new ConcurrentHashMap<>();
    private final AuthProperties properties;
    private final Clock clock;

    void requireAllowed(String email) {
        Failures current = failures.get(email);
        Instant now = clock.instant();
        if (current != null && current.count() >= properties.maxLoginAttempts()
                && now.isBefore(current.lockedUntil())) {
            long minutes = Math.max(1, java.time.Duration.between(now, current.lockedUntil()).toMinutes());
            throw new TooManyRequestsException(MessageKeys.AUTH_TOO_MANY_ATTEMPTS, Map.of("minutes", minutes));
        }
    }

    void failed(String email) {
        Instant lockedUntil = clock.instant().plus(properties.loginLockout());
        failures.merge(
            email,
            new Failures(1, lockedUntil),
            (previous, ignored) -> new Failures(previous.count() + 1, lockedUntil)
        );
    }

    void succeeded(String email) {
        failures.remove(email);
    }

    private record Failures(int count, Instant lockedUntil) {
    }
}
