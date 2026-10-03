package com.vertyll.snaptale.media;

import java.nio.file.Path;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.media")
record MediaProperties(
    @NotNull Path directory,
    @NotNull DataSize maxVideoSize,
    @NotNull DataSize maxImageSize,
    @NotNull @Positive Integer avatarSize
) {
}
