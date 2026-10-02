package com.vertyll.snaptale.common;

import java.util.Arrays;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

public enum Language {
    PL("pl"),
    EN("en");

    private final String tag;

    Language(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return tag;
    }

    public Locale locale() {
        return Locale.forLanguageTag(tag);
    }

    public static Language of(@Nullable String tag) {
        return Arrays.stream(values()).filter(language -> language.tag.equals(tag)).findFirst().orElse(PL);
    }

    public static Language of(Locale locale) {
        return of(locale.getLanguage());
    }
}
