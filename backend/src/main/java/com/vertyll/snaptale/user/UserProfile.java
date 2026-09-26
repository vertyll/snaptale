package com.vertyll.snaptale.user;

import java.time.Instant;

import org.jspecify.annotations.Nullable;

import com.vertyll.snaptale.media.MediaStorage;

public record UserProfile(long id, String name, @Nullable String bio, @Nullable String avatarUrl, Instant joinedAt) {

    static UserProfile of(UserEntity user) {
        return new UserProfile(
            user.requireId(),
            user.getName(),
            user.getBio(),
            MediaStorage.publicUrl(user.getAvatarPath()),
            user.getCreatedAt()
        );
    }
}
