package com.vertyll.snaptale.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.vertyll.snaptale.common.InvalidRequestException;
import com.vertyll.snaptale.common.MessageKeys;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class UserTokens {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();
    private final UserTokenRepository repository;
    private final Clock clock;

    String issue(long userId, TokenPurpose purpose) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.deleteByUserIdAndPurpose(userId, purpose);
        repository.save(new UserTokenEntity(userId, purpose, hash(token), clock.instant()));
        return token;
    }

    long consume(String token, TokenPurpose purpose) {
        UserTokenEntity stored = repository.findByTokenHashAndPurpose(hash(token), purpose)
            .orElseThrow(() -> new InvalidRequestException(MessageKeys.AUTH_TOKEN_INVALID));
        repository.delete(stored);
        if (stored.isExpired(clock.instant())) {
            throw new InvalidRequestException(MessageKeys.AUTH_TOKEN_INVALID);
        }
        return stored.getUserId();
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is missing from the JVM", e);
        }
    }
}
