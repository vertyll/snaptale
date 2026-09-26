package com.vertyll.snaptale.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.ValidationLimits;

record ProfileRequest(
    @NotBlank(
        message = MessageKeys.REQUIRED
    ) @Size(max = ValidationLimits.NAME_MAX_LENGTH, message = MessageKeys.TOO_LONG) String name,
    @Nullable @Size(max = ValidationLimits.BIO_MAX_LENGTH, message = MessageKeys.TOO_LONG) String bio
) {

    ProfileRequest {
        name = name.strip();
        bio = bio == null || bio.isBlank() ? null : bio.strip();
    }
}
