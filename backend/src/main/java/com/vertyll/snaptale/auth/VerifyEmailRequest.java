package com.vertyll.snaptale.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.ValidationLimits;

record VerifyEmailRequest(
    @NotBlank(
        message = MessageKeys.REQUIRED
    ) @Size(max = ValidationLimits.TOKEN_MAX_LENGTH, message = MessageKeys.INVALID_VALUE) String token
) {
}
