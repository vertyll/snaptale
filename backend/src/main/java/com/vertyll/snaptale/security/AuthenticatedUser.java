package com.vertyll.snaptale.security;

import java.io.Serial;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public final class AuthenticatedUser extends DefaultOidcUser {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;

    public AuthenticatedUser(long id, OidcUser user) {
        super(user.getAuthorities(), user.getIdToken(), user.getUserInfo(), StandardClaimNames.SUB);
        this.id = id;
    }

    public long id() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof AuthenticatedUser user && id == user.id && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
