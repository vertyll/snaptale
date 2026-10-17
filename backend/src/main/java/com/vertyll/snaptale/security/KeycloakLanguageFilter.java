package com.vertyll.snaptale.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vertyll.snaptale.common.Language;

/**
 * Keeps SnapTale's language and Keycloak's in step, both ways. A language chosen on Keycloak's pages comes back in the
 * cookie {@code ui_locale_<realm>}, which the sign-in's return takes over into the {@code lang} cookie and removes.
 * Before an account action, SnapTale's language is stored on the account, as Keycloak shows a signed-in person their
 * account's language rather than the one asked for.
 */
final class KeycloakLanguageFilter extends OncePerRequestFilter {

    private static final String CALLBACK_PATH = "/login/oauth2/code/";
    private static final String AUTHORIZATION_PATH = "/oauth2/authorization/";
    private static final Duration ONE_YEAR = Duration.ofDays(365);

    private final String signInLanguageCookie;
    private final SessionAccessTokens sessionAccessTokens;
    private final KeycloakAccount account;

    KeycloakLanguageFilter(String realm, SessionAccessTokens sessionAccessTokens, KeycloakAccount account) {
        super();
        this.signInLanguageCookie = "ui_locale_" + realm;
        this.sessionAccessTokens = sessionAccessTokens;
        this.account = account;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith(CALLBACK_PATH)) {
            takeSignInLanguage(request, response);
        } else if (path.startsWith(AUTHORIZATION_PATH)
                && request.getParameter(LocalizedAuthorizationRequestResolver.ACCOUNT_ACTION_PARAMETER) != null
                && SecurityContextHolder.getContext()
                    .getAuthentication() instanceof OAuth2AuthenticationToken session) {
            Language language = Language.of(cookie(request, LocalizedAuthorizationRequestResolver.LANGUAGE_COOKIE));
            sessionAccessTokens.current(session, request, response)
                .ifPresent(accessToken -> account.keepLanguage(accessToken, language));
        }
        chain.doFilter(request, response);
    }

    private void takeSignInLanguage(HttpServletRequest request, HttpServletResponse response) {
        String handedOver = cookie(request, signInLanguageCookie);
        if (handedOver == null) {
            return;
        }
        Language.find(handedOver).ifPresent(language -> {
            ResponseCookie lang =
                    ResponseCookie.from(LocalizedAuthorizationRequestResolver.LANGUAGE_COOKIE, language.tag())
                        .path("/")
                        .maxAge(ONE_YEAR)
                        .sameSite("Lax")
                        .secure(request.isSecure())
                        .build();
            response.addHeader(HttpHeaders.SET_COOKIE, lang.toString());
        });
        ResponseCookie.ResponseCookieBuilder removal =
                ResponseCookie.from(signInLanguageCookie, "").path("/").maxAge(0);
        sharedDomain(request.getServerName()).ifPresent(removal::domain);
        response.addHeader(HttpHeaders.SET_COOKIE, removal.build().toString());
    }

    /**
     * The domain a cookie shared with Keycloak lives under: the parent of this host. {@code localhost} and an IP
     * address have none, as every port there already shares cookies.
     */
    static Optional<String> sharedDomain(String host) {
        String[] labels = host.split("\\.");
        boolean isAddress = Arrays.stream(labels).allMatch(label -> label.chars().allMatch(Character::isDigit));
        return labels.length > 2 && !isAddress ? Optional.of(host.substring(host.indexOf('.') + 1)) : Optional.empty();
    }

    private static @Nullable String cookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
            .filter(cookie -> name.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
