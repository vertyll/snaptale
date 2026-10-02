package com.vertyll.snaptale.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("snaptale.security")
record SnaptaleSecurityProperties(@NotNull Boolean secureCookies, @NotBlank String frontendUrl) {
}
