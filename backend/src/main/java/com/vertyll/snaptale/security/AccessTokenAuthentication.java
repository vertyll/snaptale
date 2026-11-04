package com.vertyll.snaptale.security;

import java.io.Serial;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

final class AccessTokenAuthentication extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 1L;

    private final OAuth2AuthenticationToken session;
    private final Jwt accessToken;

    AccessTokenAuthentication(OAuth2AuthenticationToken session, Jwt accessToken) {
        super(List.of());
        this.session = session;
        this.accessToken = accessToken;
        setAuthenticated(true);
    }

    @Override
    public OAuth2User getPrincipal() {
        return session.getPrincipal();
    }

    @Override
    public Jwt getCredentials() {
        return accessToken;
    }
}
