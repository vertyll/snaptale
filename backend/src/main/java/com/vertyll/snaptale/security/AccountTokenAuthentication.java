package com.vertyll.snaptale.security;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

public final class AccountTokenAuthentication extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long userId;
    private final Jwt accessToken;

    public AccountTokenAuthentication(long userId, Jwt accessToken) {
        super(List.of());
        this.userId = userId;
        this.accessToken = accessToken;
        setAuthenticated(true);
    }

    public long userId() {
        return userId;
    }

    @Override
    public Long getPrincipal() {
        return userId;
    }

    @Override
    public Jwt getCredentials() {
        return accessToken;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof AccountTokenAuthentication that && super.equals(that) && userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId);
    }
}
