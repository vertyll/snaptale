package com.vertyll.snaptale.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.ValidationLimits;

record LoginRequest(
    @NotBlank(
        message = MessageKeys.REQUIRED
    ) @Size(max = ValidationLimits.EMAIL_MAX_LENGTH, message = MessageKeys.EMAIL_INVALID) String email,
    @NotBlank(
        message = MessageKeys.REQUIRED
    ) @Size(max = ValidationLimits.PASSWORD_MAX_LENGTH, message = MessageKeys.TOO_LONG) String password
) {
}
