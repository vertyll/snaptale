package com.vertyll.snaptale.auth;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.vertyll.snaptale.security.AuthenticatedUser;
import com.vertyll.snaptale.user.UserAccounts;

@Service
class KeycloakUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final UserAccounts accounts;

    KeycloakUserService(UserAccounts accounts) {
        this.accounts = accounts;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser user = Objects.requireNonNull(delegate.loadUser(request), "Keycloak returned no user");
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_email"));
        }
        long id = accounts.signIn(user.getName(), email, displayName(user, email));
        return new AuthenticatedUser(id, user);
    }

    private static String displayName(OidcUser user, String email) {
        return firstPresent(user.getFullName(), user.getPreferredUsername(), email.substring(0, email.indexOf('@')));
    }

    private static String firstPresent(@Nullable String first, @Nullable String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null && !second.isBlank() ? second : fallback;
    }
}
