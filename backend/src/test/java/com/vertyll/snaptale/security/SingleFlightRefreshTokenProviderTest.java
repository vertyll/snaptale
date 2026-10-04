package com.vertyll.snaptale.security;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SingleFlightRefreshTokenProviderTest {
    private static final Instant NOW = Instant.parse("2026-10-03T10:00:00Z");
    private static final ClientRegistration REGISTRATION = ClientRegistration.withRegistrationId("keycloak")
        .clientId("snaptale-backend")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://localhost/callback")
        .authorizationUri("http://localhost/auth")
        .tokenUri("http://localhost/token")
        .build();

    @Test
    void concurrentRequestsShareOneRefresh() throws InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        OAuth2AuthorizedClientProvider slow = context -> {
            calls.incrementAndGet();
            entered.countDown();
            awaitQuietly(release);
            return client("access-2", "refresh-2");
        };
        SingleFlightRefreshTokenProvider provider = new SingleFlightRefreshTokenProvider(slow, fixedClock());

        CompletableFuture<OAuth2AuthorizedClient> first =
                CompletableFuture.supplyAsync(() -> provider.authorize(context(client("access-1", "refresh-1"))));
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
        CompletableFuture<OAuth2AuthorizedClient> second =
                CompletableFuture.supplyAsync(() -> provider.authorize(context(client("access-1", "refresh-1"))));
        release.countDown();

        assertThat(first.get(5, TimeUnit.SECONDS).getAccessToken().getTokenValue()).isEqualTo("access-2");
        assertThat(second.get(5, TimeUnit.SECONDS).getAccessToken().getTokenValue()).isEqualTo("access-2");
        assertThat(calls).hasValue(1);
    }

    @Test
    void aStaleSessionGetsTheTokensAlreadyIssuedForItsRefreshToken() {
        AtomicInteger calls = new AtomicInteger();
        OAuth2AuthorizedClientProvider counting = context -> {
            calls.incrementAndGet();
            return client("access-2", "refresh-2");
        };
        SingleFlightRefreshTokenProvider provider = new SingleFlightRefreshTokenProvider(counting, fixedClock());

        provider.authorize(context(client("access-1", "refresh-1")));
        OAuth2AuthorizedClient stale = provider.authorize(context(client("access-1", "refresh-1")));

        assertThat(stale).isNotNull();
        assertThat(stale.getAccessToken().getTokenValue()).isEqualTo("access-2");
        assertThat(calls).hasValue(1);
    }

    @Test
    void aRefusedRefreshIsNotRemembered() {
        AtomicInteger calls = new AtomicInteger();
        OAuth2AuthorizedClientProvider refusing = context -> {
            calls.incrementAndGet();
            throw new OAuth2AuthorizationException(new OAuth2Error("invalid_grant"));
        };
        SingleFlightRefreshTokenProvider provider = new SingleFlightRefreshTokenProvider(refusing, fixedClock());
        OAuth2AuthorizationContext context = context(client("access-1", "refresh-1"));

        assertThatThrownBy(() -> provider.authorize(context)).isInstanceOf(OAuth2AuthorizationException.class);
        assertThatThrownBy(() -> provider.authorize(context)).isInstanceOf(OAuth2AuthorizationException.class);
        assertThat(calls).hasValue(2);
    }

    private static OAuth2AuthorizationContext context(OAuth2AuthorizedClient client) {
        return OAuth2AuthorizationContext.withAuthorizedClient(client)
            .principal(new TestingAuthenticationToken("user", "n/a"))
            .build();
    }

    private static OAuth2AuthorizedClient client(String access, String refresh) {
        return new OAuth2AuthorizedClient(
            REGISTRATION,
            "user",
            new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, access, NOW, NOW.plusSeconds(300)),
            new OAuth2RefreshToken(refresh, NOW)
        );
    }

    private static Clock fixedClock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}
