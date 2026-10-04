package com.vertyll.snaptale.security;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;

import com.vertyll.snaptale.security.SharedRefreshes.TokenPair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SharedRefreshesTest {
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    private static LettuceConnectionFactory connections;
    private static StringRedisTemplate redis;

    @BeforeAll
    static void startRedis() {
        REDIS.start();
        connections = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connections.afterPropertiesSet();
        connections.start();
        redis = new StringRedisTemplate(connections);
    }

    @AfterAll
    static void stopRedis() {
        connections.destroy();
        REDIS.stop();
    }

    @Test
    void twoReplicasRefreshingOneTokenReachKeycloakOnce() throws InterruptedException, ExecutionException, TimeoutException {
        SharedRefreshes first = new SharedRefreshes(redis, new RedisKeyProperties("test-a"));
        SharedRefreshes second = new SharedRefreshes(redis, new RedisKeyProperties("test-a"));
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        CompletableFuture<TokenPair> leader = CompletableFuture.supplyAsync(() -> first.refresh("refresh-1", () -> {
            calls.incrementAndGet();
            entered.countDown();
            await(release);
            return pair("access-2", "refresh-2");
        }));
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
        CompletableFuture<TokenPair> follower = CompletableFuture.supplyAsync(() -> second.refresh("refresh-1", () -> {
            calls.incrementAndGet();
            return pair("access-3", "refresh-3");
        }));
        release.countDown();

        assertThat(leader.get(10, TimeUnit.SECONDS).refreshToken()).isEqualTo("refresh-2");
        assertThat(follower.get(10, TimeUnit.SECONDS).refreshToken()).isEqualTo("refresh-2");
        assertThat(calls).hasValue(1);
    }

    @Test
    void aStaleRequestReceivesTheTokensAlreadyIssued() {
        SharedRefreshes replica = new SharedRefreshes(redis, new RedisKeyProperties("test-b"));
        replica.refresh("refresh-1", () -> pair("access-2", "refresh-2"));

        TokenPair stale = new SharedRefreshes(redis, new RedisKeyProperties("test-b")).refresh("refresh-1", () -> {
            throw new IllegalStateException("Keycloak must not be asked twice");
        });

        assertThat(stale.accessToken()).isEqualTo("access-2");
    }

    @Test
    void aRefusedRefreshIsNotShared() {
        SharedRefreshes replica = new SharedRefreshes(redis, new RedisKeyProperties("test-c"));
        assertThatThrownBy(() -> replica.refresh("refresh-1", () -> {
            throw new IllegalStateException("refused");
        })).isInstanceOf(IllegalStateException.class);

        TokenPair retried = replica.refresh("refresh-1", () -> pair("access-2", "refresh-2"));

        assertThat(retried.refreshToken()).isEqualTo("refresh-2");
    }

    @Test
    void keysCarryTheApplicationPrefixAndNeverTheToken() {
        new SharedRefreshes(redis, new RedisKeyProperties("test-d")).refresh("secret-refresh", () -> pair("access", "refresh"));

        assertThat(redis.keys("test-d:refresh-result:*")).hasSize(1);
        assertThat(redis.keys("*secret-refresh*")).isEmpty();
    }

    private static TokenPair pair(String access, String refresh) {
        Instant now = Instant.now();
        return new TokenPair(access, refresh, now, now.plusSeconds(300));
    }

    private static void await(CountDownLatch latch) {
        try {
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}
