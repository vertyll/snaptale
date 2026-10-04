package com.vertyll.snaptale;

import java.time.Instant;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration(proxyBeanMethods = false)
public class TestAccessTokens {

    public static final String REJECTED = "rejected-access-token";

    public static String of(String keycloakId) {
        return "access-token-" + keycloakId;
    }

    @Bean
    @Primary
    JwtDecoder testAccessTokenDecoder() {
        return token -> {
            if (!token.startsWith("access-token-")) {
                throw new BadJwtException("Unknown test token");
            }
            Instant now = Instant.now();
            String keycloakId = token.substring("access-token-".length());
            return Jwt.withTokenValue(token)
                .header("alg", "none")
                .subject(keycloakId)
                .claim("email", keycloakId + "@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();
        };
    }
}
