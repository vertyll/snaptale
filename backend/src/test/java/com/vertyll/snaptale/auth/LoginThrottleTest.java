package com.vertyll.snaptale.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.vertyll.snaptale.common.TooManyRequestsException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginThrottleTest {

    private static final String EMAIL = "ktos@example.com";
    private static final Instant NOW = Instant.parse("2026-09-26T10:00:00Z");

    @Test
    void locksAfterTheLimitAndReleasesAfterTheLockout() {
        AuthProperties properties =
                new AuthProperties("http://localhost:3000", "a@b.pl", "SnapTale", 3, Duration.ofMinutes(15));
        LoginThrottle throttle = new LoginThrottle(properties, Clock.fixed(NOW, ZoneOffset.UTC));
        for (int attempt = 0; attempt < 3; attempt++) {
            throttle.failed(EMAIL);
        }

        assertThatThrownBy(() -> throttle.requireAllowed(EMAIL)).isInstanceOf(TooManyRequestsException.class);
        LoginThrottle later =
                new LoginThrottle(properties, Clock.fixed(NOW.plus(Duration.ofMinutes(16)), ZoneOffset.UTC));
        assertThatCode(() -> later.requireAllowed(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void successClearsTheFailures() {
        AuthProperties properties =
                new AuthProperties("http://localhost:3000", "a@b.pl", "SnapTale", 1, Duration.ofMinutes(15));
        LoginThrottle throttle = new LoginThrottle(properties, Clock.fixed(NOW, ZoneOffset.UTC));
        throttle.failed(EMAIL);
        throttle.succeeded(EMAIL);

        assertThatCode(() -> throttle.requireAllowed(EMAIL)).doesNotThrowAnyException();
    }
}
