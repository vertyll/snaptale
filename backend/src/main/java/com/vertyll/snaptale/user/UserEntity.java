package com.vertyll.snaptale.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private @Nullable String bio;

    @Column(name = "avatar_path")
    private @Nullable String avatarPath;

    @Column(name = "email_verified_at")
    private @Nullable Instant emailVerifiedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {
        this.name = "";
        this.email = "";
        this.passwordHash = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    UserEntity(String name, String email, String passwordHash, Instant now) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = now;
        this.updatedAt = now;
    }

    long requireId() {
        if (id == null) {
            throw new IllegalStateException("User has not been saved yet");
        }
        return id;
    }

    void updateProfile(String name, @Nullable String bio, Instant now) {
        this.name = name;
        this.bio = bio;
        this.updatedAt = now;
    }

    @Nullable String replaceAvatar(String avatarPath, Instant now) {
        String previous = this.avatarPath;
        this.avatarPath = avatarPath;
        this.updatedAt = now;
        return previous;
    }

    void changePassword(String passwordHash, Instant now) {
        this.passwordHash = passwordHash;
        this.updatedAt = now;
    }

    void markEmailVerified(Instant now) {
        if (emailVerifiedAt == null) {
            this.emailVerifiedAt = now;
            this.updatedAt = now;
        }
    }
}
