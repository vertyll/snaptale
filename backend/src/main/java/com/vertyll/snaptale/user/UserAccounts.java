package com.vertyll.snaptale.user;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.snaptale.common.ValidationLimits;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccounts {

    private final UserRepository repository;
    private final Clock clock;

    public long signIn(String keycloakId, String email, String name) {
        Instant now = clock.instant();
        return repository.findByKeycloakId(keycloakId).map(user -> {
            user.syncEmail(email, now);
            return user.requireId();
        }).orElseGet(() -> repository.save(new UserEntity(keycloakId, displayName(name), email, now)).requireId());
    }

    private static String displayName(String name) {
        String stripped = name.strip();
        return stripped.length() <= ValidationLimits.NAME_MAX_LENGTH ? stripped
                : stripped.substring(0, ValidationLimits.NAME_MAX_LENGTH).strip();
    }
}
