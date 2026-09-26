package com.vertyll.snaptale.common;

@SuppressWarnings("PMD.DataClass")
public final class MessageKeys {

    public static final String REQUIRED = "validation.required";
    public static final String TOO_LONG = "validation.tooLong";
    public static final String TOO_SHORT = "validation.tooShort";
    public static final String EMAIL_INVALID = "validation.email";
    public static final String INVALID_VALUE = "validation.invalidValue";

    public static final String INVALID_FORM = "errors.invalidForm";
    public static final String DUPLICATE_ENTRY = "errors.duplicateEntry";
    public static final String UPLOAD_TOO_LARGE = "errors.uploadTooLarge";
    public static final String HTTP_STATUS_PREFIX = "errors.status.";

    public static final String AUTH_BAD_CREDENTIALS = "errors.auth.badCredentials";
    public static final String AUTH_TOO_MANY_ATTEMPTS = "errors.auth.tooManyAttempts";
    public static final String AUTH_EMAIL_TAKEN = "errors.auth.emailTaken";
    public static final String AUTH_TOKEN_INVALID = "errors.auth.tokenInvalid";

    public static final String USER_NOT_FOUND = "errors.user.notFound";
    public static final String FOLLOW_SELF = "errors.follow.self";

    public static final String POST_NOT_FOUND = "errors.post.notFound";
    public static final String POST_NOT_OWNER = "errors.post.notOwner";
    public static final String COMMENT_NOT_FOUND = "errors.comment.notFound";
    public static final String COMMENT_NOT_OWNER = "errors.comment.notOwner";

    public static final String MEDIA_VIDEO_REQUIRED = "errors.media.videoRequired";
    public static final String MEDIA_VIDEO_UNSUPPORTED = "errors.media.videoUnsupported";
    public static final String MEDIA_VIDEO_TOO_LARGE = "errors.media.videoTooLarge";
    public static final String MEDIA_IMAGE_REQUIRED = "errors.media.imageRequired";
    public static final String MEDIA_IMAGE_UNSUPPORTED = "errors.media.imageUnsupported";
    public static final String MEDIA_IMAGE_TOO_LARGE = "errors.media.imageTooLarge";
    public static final String MEDIA_CROP_INVALID = "errors.media.cropInvalid";

    private MessageKeys() {
    }
}
