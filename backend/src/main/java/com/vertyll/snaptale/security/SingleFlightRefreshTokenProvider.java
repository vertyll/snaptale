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
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

final class SingleFlightRefreshTokenProvider implements OAuth2AuthorizedClientProvider {
    private static final Duration REUSE_WINDOW = Duration.ofSeconds(30);

    private final OAuth2AuthorizedClientProvider refresh;
    private final Clock clock;
    private final Map<String, Refresh> refreshes = new ConcurrentHashMap<>();

    SingleFlightRefreshTokenProvider(OAuth2AuthorizedClientProvider refresh, Clock clock) {
        this.refresh = refresh;
        this.clock = clock;
    }

    @Override
    public @Nullable OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        OAuth2AuthorizedClient current = context.getAuthorizedClient();
        OAuth2RefreshToken refreshToken = current == null ? null : current.getRefreshToken();
        if (refreshToken == null) {
            return null;
        }
        forgetOldRefreshes();

        String key = refreshToken.getTokenValue();
        Refresh mine = new Refresh();
        Refresh running = refreshes.putIfAbsent(key, mine);
        if (running != null) {
            Outcome outcome = running.await();
            return outcome.completed() ? outcome.client() : refresh.authorize(context);
        }

        boolean finished = false;
        try {
            OAuth2AuthorizedClient refreshed = refresh.authorize(context);
            mine.finish(refreshed, clock.instant());
            finished = true;
            if (refreshed == null) {
                refreshes.remove(key, mine);
            }
            return refreshed;
        } finally {
            if (!finished) {
                refreshes.remove(key, mine);
                mine.abandon();
            }
        }
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
