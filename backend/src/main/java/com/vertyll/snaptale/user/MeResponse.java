package com.vertyll.snaptale.user;

import org.jspecify.annotations.Nullable;

import com.vertyll.snaptale.media.MediaStorage;

record MeResponse(@Nullable Me user) {

    static MeResponse anonymous() {
        return new MeResponse(null);
    }

    record Me(
        long id,
        String name,
        String email,
        @Nullable String bio,
        @Nullable String avatarUrl,
        boolean emailVerified
    ) {

        static Me of(UserEntity user) {
            return new Me(
                user.requireId(),
                user.getName(),
                user.getEmail(),
                user.getBio(),
                MediaStorage.publicUrl(user.getAvatarPath()),
                user.getEmailVerifiedAt() != null
            );
        }
    }
}
