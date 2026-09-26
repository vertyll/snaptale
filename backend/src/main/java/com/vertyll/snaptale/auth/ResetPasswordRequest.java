package com.vertyll.snaptale.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.ValidationLimits;

record ResetPasswordRequest(
    @NotBlank(
        message = MessageKeys.REQUIRED
    ) @Size(max = ValidationLimits.TOKEN_MAX_LENGTH, message = MessageKeys.INVALID_VALUE) String token,
    @NotBlank(message = MessageKeys.REQUIRED) @Size(
        min = ValidationLimits.PASSWORD_MIN_LENGTH,
        message = MessageKeys.TOO_SHORT
    ) @Size(max = ValidationLimits.PASSWORD_MAX_LENGTH, message = MessageKeys.TOO_LONG) String password
) {
}
