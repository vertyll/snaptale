package com.vertyll.snaptale;

import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.mysql.MySQLContainer;

// Spring stops the containers when the test context closes.
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer mySqlContainer() {
        return new MySQLContainer("mysql:9.7.4").withStartupTimeout(Duration.ofMinutes(2))
            .withStartupAttempts(3)
            .withDatabaseName("snaptale");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>("redis:8-alpine").withStartupTimeout(Duration.ofMinutes(2))
            .withStartupAttempts(3)
            .withExposedPorts(6379);
    }
}
