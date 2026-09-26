package com.vertyll.snaptale.user;

import org.jspecify.annotations.Nullable;

import com.vertyll.snaptale.media.MediaStorage;

public record UserSummary(long id, String name, @Nullable String avatarUrl) {

    static UserSummary of(UserEntity user) {
        return new UserSummary(user.requireId(), user.getName(), MediaStorage.publicUrl(user.getAvatarPath()));
    }
}
