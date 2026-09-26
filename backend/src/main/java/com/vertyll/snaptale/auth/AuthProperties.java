package com.vertyll.snaptale.auth;

import java.time.Duration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("snaptale.auth")
record AuthProperties(
    @NotBlank @Pattern(
        regexp = "https?://.+[^/]",
        message = "must be an http(s) URL without a trailing slash"
    ) String frontendUrl,
    @NotBlank @Email String mailFrom,
    @NotBlank String mailFromName,
    @NotNull @Positive Integer maxLoginAttempts,
    @NotNull Duration loginLockout
) {
}
