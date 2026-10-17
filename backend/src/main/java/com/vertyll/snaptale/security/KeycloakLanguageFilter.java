package com.vertyll.snaptale.security;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vertyll.snaptale.common.Language;
import com.vertyll.snaptale.keycloak.KeycloakAccounts;

/**
 * Before an account action, stores SnapTale's language on the account, as Keycloak shows a signed-in person their
 * account's language rather than the one asked for. The language comes from the {@code lang} cookie, which the front-end
 * keeps in step with the one chosen on Keycloak's pages.
 */
final class KeycloakLanguageFilter extends OncePerRequestFilter {

    private final SessionAccessTokens sessionAccessTokens;
    private final KeycloakAccounts accounts;

    KeycloakLanguageFilter(SessionAccessTokens sessionAccessTokens, KeycloakAccounts accounts) {
        super();
        this.sessionAccessTokens = sessionAccessTokens;
        this.accounts = accounts;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        if (request.getRequestURI()
            .startsWith(OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI)
                && request.getParameter(LocalizedAuthorizationRequestResolver.ACCOUNT_ACTION_PARAMETER) != null
                && SecurityContextHolder.getContext()
                    .getAuthentication() instanceof OAuth2AuthenticationToken session) {
            Language language = Language.of(cookie(request, LocalizedAuthorizationRequestResolver.LANGUAGE_COOKIE));
            sessionAccessTokens.current(session, request, response)
                .ifPresent(accessToken -> accounts.keepLanguage(accessToken, language));
        }
        chain.doFilter(request, response);
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
