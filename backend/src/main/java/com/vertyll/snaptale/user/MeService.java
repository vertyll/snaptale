package com.vertyll.snaptale.user;

import java.time.Clock;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vertyll.snaptale.media.AvatarCrop;
import com.vertyll.snaptale.media.MediaStorage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class MeService {

    private final UserDirectory directory;
    private final MediaStorage media;
    private final Clock clock;

    @Transactional(readOnly = true)
    MeResponse.Me me(long userId) {
        return MeResponse.Me.of(directory.get(userId));
    }

    @Transactional
    MeResponse.Me updateProfile(long userId, ProfileRequest request) {
        UserEntity user = directory.get(userId);
        user.updateProfile(request.name(), request.bio(), clock.instant());
        return MeResponse.Me.of(user);
    }

    @Transactional
    MeResponse.Me changeAvatar(long userId, @Nullable MultipartFile image, AvatarCrop crop) {
        UserEntity user = directory.get(userId);
        String path = media.storeAvatar(image, crop);
        media.deleteAfterCommit(user.replaceAvatar(path, clock.instant()));
        return MeResponse.Me.of(user);
    }
}
