package com.vertyll.snaptale.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

final class SingleFlightRefreshTokenProvider implements OAuth2AuthorizedClientProvider {
    private static final Duration REUSE_WINDOW = Duration.ofSeconds(30);
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(60);

    private final OAuth2AuthorizedClientProvider refresh;
    private final SharedRefreshes sharedRefreshes;
    private final Clock clock;
    private final Map<String, Refresh> refreshes = new ConcurrentHashMap<>();

    SingleFlightRefreshTokenProvider(
        OAuth2AuthorizedClientProvider refresh,
        SharedRefreshes sharedRefreshes,
        Clock clock
    ) {
        this.refresh = refresh;
        this.sharedRefreshes = sharedRefreshes;
        this.clock = clock;
    }

    @Override
    public @Nullable OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        OAuth2AuthorizedClient current = context.getAuthorizedClient();
        OAuth2RefreshToken refreshToken = current == null ? null : current.getRefreshToken();
        if (current == null || refreshToken == null || !expiresSoon(current.getAccessToken())) {
            return refresh.authorize(context);
        }
        forgetOldRefreshes();

        String key = refreshToken.getTokenValue();
        Refresh mine = new Refresh();
        Refresh running = refreshes.putIfAbsent(key, mine);
        if (running != null) {
            Outcome outcome = running.await();
            return outcome.completed() ? outcome.client() : refreshShared(context, current, refreshToken);
        }

        boolean finished = false;
        try {
            OAuth2AuthorizedClient refreshed = refreshShared(context, current, refreshToken);
            mine.finish(refreshed, clock.instant());
            finished = true;
            return refreshed;
        } finally {
            if (!finished) {
                refreshes.remove(key, mine);
                mine.abandon();
            }
        }
    }

    private OAuth2AuthorizedClient refreshShared(
        OAuth2AuthorizationContext context,
        OAuth2AuthorizedClient current,
        OAuth2RefreshToken refreshToken
    ) {
        SharedRefreshes.TokenPair tokens = sharedRefreshes.refresh(refreshToken.getTokenValue(), () -> {
            OAuth2AuthorizedClient refreshed = refresh.authorize(context);
            if (refreshed == null) {
                throw new IllegalStateException("The refresh provider declined an expired access token");
            }
            OAuth2RefreshToken next = refreshed.getRefreshToken();
            OAuth2AccessToken access = refreshed.getAccessToken();
            Instant issuedAt = access.getIssuedAt() == null ? clock.instant() : access.getIssuedAt();
            Instant expiresAt = access.getExpiresAt() == null ? issuedAt : access.getExpiresAt();
            return new SharedRefreshes.TokenPair(
                access.getTokenValue(),
                next == null ? refreshToken.getTokenValue() : next.getTokenValue(),
                issuedAt,
                expiresAt
            );
        });
        return new OAuth2AuthorizedClient(
            current.getClientRegistration(),
            current.getPrincipalName(),
            new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokens.accessToken(),
                tokens.issuedAt(),
                tokens.expiresAt(),
                current.getAccessToken().getScopes()
            ),
            new OAuth2RefreshToken(tokens.refreshToken(), tokens.issuedAt())
        );
    }

    private boolean expiresSoon(OAuth2AccessToken accessToken) {
        Instant expiresAt = accessToken.getExpiresAt();
        return expiresAt != null && !clock.instant().isBefore(expiresAt.minus(CLOCK_SKEW));
    }

    private void forgetOldRefreshes() {
        Instant oldest = clock.instant().minus(REUSE_WINDOW);
        refreshes.values().removeIf(pending -> pending.finishedBefore(oldest));
    }

    private record Outcome(@Nullable OAuth2AuthorizedClient client, boolean completed, Instant at) {
    }

    private static final class Refresh {
        private final CompletableFuture<Outcome> result = new CompletableFuture<>();

        void finish(@Nullable OAuth2AuthorizedClient client, Instant at) {
            result.complete(new Outcome(client, true, at));
        }

        void abandon() {
            result.complete(new Outcome(null, false, Instant.MIN));
        }

        Outcome await() {
            return result.join();
        }

        boolean finishedBefore(Instant instant) {
            Outcome outcome = result.getNow(null);
            return outcome != null && outcome.at().isBefore(instant);
        }
    }
}
