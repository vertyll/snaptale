package com.vertyll.snaptale.common;

@SuppressWarnings("PMD.DataClass")
public final class ValidationLimits {

    public static final int NAME_MAX_LENGTH = 50;
    public static final int EMAIL_MAX_LENGTH = 254;
    public static final int BIO_MAX_LENGTH = 160;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 72;
    public static final int POST_TEXT_MAX_LENGTH = 300;
    public static final int COMMENT_MAX_LENGTH = 500;
    public static final int TOKEN_MAX_LENGTH = 100;

    private ValidationLimits() {
    }
}
