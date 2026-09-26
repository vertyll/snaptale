package com.vertyll.snaptale.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.ValidationLimits;

record NewPostForm(
    @NotBlank(
        message = MessageKeys.REQUIRED
    ) @Size(max = ValidationLimits.POST_TEXT_MAX_LENGTH, message = MessageKeys.TOO_LONG) String text
) {

    NewPostForm {
        text = text.strip();
    }
}
