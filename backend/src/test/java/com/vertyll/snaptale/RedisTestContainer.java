package com.vertyll.snaptale;

import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class RedisTestContainer {

    // Spring stops the container when the test context closes.
    @Bean
    @SuppressWarnings("resource")
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>("redis:8-alpine").withStartupTimeout(Duration.ofMinutes(2))
            .withStartupAttempts(3)
            .withExposedPorts(6379);
    }
}
