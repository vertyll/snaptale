package com.vertyll.snaptale.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.mail")
record MailProperties(@NotBlank String from) {
}
