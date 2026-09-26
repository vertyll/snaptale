package com.vertyll.snaptale.media;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.vertyll.snaptale.common.MessageKeys;

public record AvatarCrop(
    @NotNull(message = MessageKeys.REQUIRED) @PositiveOrZero(message = MessageKeys.INVALID_VALUE) Integer x,
    @NotNull(message = MessageKeys.REQUIRED) @PositiveOrZero(message = MessageKeys.INVALID_VALUE) Integer y,
    @NotNull(message = MessageKeys.REQUIRED) @Positive(message = MessageKeys.INVALID_VALUE) Integer width,
    @NotNull(message = MessageKeys.REQUIRED) @Positive(message = MessageKeys.INVALID_VALUE) Integer height
) {
}
