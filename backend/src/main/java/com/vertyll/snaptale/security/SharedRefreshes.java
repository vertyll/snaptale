package com.vertyll.snaptale.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class SharedRefreshes {
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final Duration RESULT_TTL = Duration.ofSeconds(30);
    private static final Duration WAIT_INTERVAL = Duration.ofMillis(100);
    private static final int WAIT_ATTEMPTS = 50;
    private static final String SEPARATOR = "\n";
    private static final int FIELDS = 4;

    private final @Nullable StringRedisTemplate redis;
    private final String keyPrefix;

    @Autowired
    SharedRefreshes(StringRedisTemplate redis, RedisKeyProperties properties) {
        this.redis = redis;
        this.keyPrefix = properties.keyPrefix();
    }

    private SharedRefreshes() {
        this.redis = null;
        this.keyPrefix = "";
    }

    static SharedRefreshes inProcessOnly() {
        return new SharedRefreshes();
    }

    TokenPair refresh(String refreshToken, Supplier<TokenPair> keycloak) {
        StringRedisTemplate store = redis;
        if (store == null) {
            return keycloak.get();
        }
        String id = sha256(refreshToken);
        String lockKey = keyPrefix + ":refresh-lock:" + id;
        String resultKey = keyPrefix + ":refresh-result:" + id;
        Optional<TokenPair> shared;
        boolean leader;
        try {
            shared = read(store, resultKey);
            leader = shared.isEmpty() && Boolean.TRUE.equals(store.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL));
        } catch (DataAccessException e) {
            log.warn("Redis unavailable, refreshing without coordinating replicas: {}", e.getMessage());
            return keycloak.get();
        }
        if (shared.isPresent()) {
            return shared.get();
        }
        if (leader) {
            return lead(store, lockKey, resultKey, keycloak);
        }
        return awaitOtherReplica(store, lockKey, resultKey).orElseGet(keycloak);
    }

    private TokenPair lead(StringRedisTemplate store, String lockKey, String resultKey, Supplier<TokenPair> keycloak) {
        boolean refreshed = false;
        TokenPair pair;
        try {
            pair = keycloak.get();
            refreshed = true;
        } finally {
            if (!refreshed) {
                release(store, lockKey);
            }
        }
        try {
            store.opsForValue().set(resultKey, pair.serialize(), RESULT_TTL);
        } catch (DataAccessException e) {
            log.warn("Could not share the refreshed tokens with other replicas: {}", e.getMessage());
        }
        return pair;
    }

    private Optional<TokenPair> awaitOtherReplica(StringRedisTemplate store, String lockKey, String resultKey) {
        try {
            for (int attempt = 0; attempt < WAIT_ATTEMPTS; attempt++) {
                Thread.sleep(WAIT_INTERVAL);
                Optional<TokenPair> shared = read(store, resultKey);
                if (shared.isPresent() || !Boolean.TRUE.equals(store.hasKey(lockKey))) {
                    return shared;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DataAccessException e) {
            log.warn("Redis unavailable while waiting for another replica's refresh: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private static Optional<TokenPair> read(StringRedisTemplate store, String resultKey) {
        String value = store.opsForValue().get(resultKey);
        if (value == null) {
            return Optional.empty();
        }
        return TokenPair.parse(value);
    }

    private static void release(StringRedisTemplate store, String lockKey) {
        try {
            store.delete(lockKey);
        } catch (DataAccessException e) {
            log.warn("Could not release the refresh lock, it expires on its own: {}", e.getMessage());
        }
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by every Java platform", e);
        }
    }

    record TokenPair(String accessToken, String refreshToken, Instant issuedAt, Instant expiresAt) {

        static Optional<TokenPair> parse(String value) {
            String[] parts = value.split(SEPARATOR, -1);
            if (parts.length != FIELDS) {
                return Optional.empty();
            }
            return Optional.of(
                new TokenPair(
                    parts[0],
                    parts[1],
                    Instant.ofEpochMilli(Long.parseLong(parts[2])),
                    Instant.ofEpochMilli(Long.parseLong(parts[3]))
                )
            );
        }

        String serialize() {
            return String.join(
                SEPARATOR,
                accessToken,
                refreshToken,
                Long.toString(issuedAt.toEpochMilli()),
                Long.toString(expiresAt.toEpochMilli())
            );
        }

        @Override
        public String toString() {
            return "TokenPair[accessToken=***, refreshToken=***, issuedAt=" + issuedAt + ", expiresAt=" + expiresAt
                    + "]";
        }
    }
}
