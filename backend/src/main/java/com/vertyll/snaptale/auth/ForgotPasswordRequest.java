package com.vertyll.snaptale.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.ValidationLimits;

record ForgotPasswordRequest(
    @NotBlank(message = MessageKeys.REQUIRED) @Email(
        message = MessageKeys.EMAIL_INVALID
    ) @Size(max = ValidationLimits.EMAIL_MAX_LENGTH, message = MessageKeys.EMAIL_INVALID) String email
) {
}
