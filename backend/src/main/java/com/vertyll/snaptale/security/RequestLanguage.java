package com.vertyll.snaptale.security;

import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import com.vertyll.snaptale.common.Language;

/**
 * The language the reader uses here, which the front-end keeps in the {@code lang} cookie in step with the one
 * chosen on Keycloak's pages, and which Keycloak's pages are opened in.
 */
final class RequestLanguage {

    static final String COOKIE = "lang";

    private RequestLanguage() {
    }

    /** The language of this request's cookie, or the default when it names none the application has. */
    static Language of(HttpServletRequest request) {
        return Language.of(
            Optional.ofNullable(request.getCookies())
                .flatMap(
                    cookies -> Arrays.stream(cookies).filter(cookie -> COOKIE.equals(cookie.getName())).findFirst()
                )
                .map(Cookie::getValue)
                .orElse(null)
        );
    }
}
