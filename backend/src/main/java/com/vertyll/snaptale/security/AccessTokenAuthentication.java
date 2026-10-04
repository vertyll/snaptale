package com.vertyll.snaptale.security;

import java.io.Serial;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

final class AccessTokenAuthentication extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 1L;

    private final OAuth2User user;
    private final Jwt accessToken;

    AccessTokenAuthentication(OAuth2User user, Jwt accessToken) {
        super(List.of());
        this.user = user;
        this.accessToken = accessToken;
        setAuthenticated(true);
    }

    @Override
    public OAuth2User getPrincipal() {
        return user;
    }

    @Override
    public Jwt getCredentials() {
        return accessToken;
    }
}
