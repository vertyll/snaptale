package com.vertyll.snaptale.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

import com.vertyll.snaptale.common.Language;
import com.vertyll.snaptale.keycloak.KeycloakAccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakLanguageFilterTest {
    private static final Jwt ACCESS_TOKEN =
            Jwt.withTokenValue("access").header("alg", "none").claim("sub", "person").build();

    private final SessionAccessTokens sessionAccessTokens = mock(SessionAccessTokens.class);
    private final KeycloakAccounts accounts = mock(KeycloakAccounts.class);
    private final KeycloakLanguageFilter filter = new KeycloakLanguageFilter(sessionAccessTokens, accounts);

    @AfterEach
    void forgetTheSession() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anAccountActionStoresTheLanguageUsedHereOnTheAccount() throws ServletException, IOException {
        OAuth2AuthenticationToken session = signedIn();
        when(sessionAccessTokens.current(eq(session), any(), any())).thenReturn(Optional.of(ACCESS_TOKEN));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(accountAction("UPDATE_PASSWORD"), new MockHttpServletResponse(), chain);

        verify(accounts).keepLanguage(ACCESS_TOKEN, Language.EN);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void anActionKeycloakIsNotAskedForLeavesTheAccountAsItIs() throws ServletException, IOException {
        signedIn();

        filter.doFilter(accountAction("VERIFY_EMAIL"), new MockHttpServletResponse(), new MockFilterChain());

        verify(accounts, never()).keepLanguage(any(), any());
    }

    @Test
    void aSignInWithoutASessionLeavesTheAccountAsItIs() throws ServletException, IOException {
        filter.doFilter(accountAction("UPDATE_PASSWORD"), new MockHttpServletResponse(), new MockFilterChain());

        verify(accounts, never()).keepLanguage(any(), any());
    }

    private static MockHttpServletRequest accountAction(String action) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/keycloak");
        request.setParameter("kc_action", action);
        request.setCookies(new Cookie(RequestLanguage.COOKIE, "en"));
        return request;
    }

    private static OAuth2AuthenticationToken signedIn() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        OAuth2AuthenticationToken session = new OAuth2AuthenticationToken(
            new DefaultOAuth2User(authorities, Map.of("sub", "person"), "sub"),
            authorities,
            "keycloak"
        );
        SecurityContextHolder.getContext().setAuthentication(session);
        return session;
    }
}
