package com.vertyll.snaptale.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.Getter;

@Getter
@Entity
@Table(name = "user_tokens")
class UserTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenPurpose purpose;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserTokenEntity() {
        this.purpose = TokenPurpose.PASSWORD_RESET;
        this.tokenHash = "";
        this.expiresAt = Instant.EPOCH;
        this.createdAt = Instant.EPOCH;
    }

    UserTokenEntity(long userId, TokenPurpose purpose, String tokenHash, Instant createdAt) {
        this.userId = userId;
        this.purpose = purpose;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = createdAt.plus(purpose.validity());
    }

    boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
