package com.vertyll.snaptale.user;

import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vertyll.snaptale.media.AvatarCrop;
import com.vertyll.snaptale.security.CurrentUser;
import com.vertyll.snaptale.security.Viewer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
class MeController {

    private final MeService service;

    @GetMapping
    MeResponse me(Viewer viewer) {
        return viewer.userId().map(service::me).map(MeResponse::new).orElseGet(MeResponse::anonymous);
    }

    @PatchMapping
    MeResponse.Me updateProfile(CurrentUser user, @Valid @RequestBody ProfileRequest request) {
        return service.updateProfile(user.id(), request);
    }

    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    MeResponse.Me changeAvatar(
        CurrentUser user,
        @RequestPart(name = "image", required = false) @Nullable MultipartFile image,
        @Valid @ModelAttribute AvatarCrop crop
    ) {
        return service.changeAvatar(user.id(), image, crop);
    }
}
