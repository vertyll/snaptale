package com.vertyll.snaptale.auth;

import java.util.Objects;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import com.vertyll.snaptale.security.AccountTokenAuthentication;
import com.vertyll.snaptale.user.UserAccounts;

@Component
class BearerTokenAccounts implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserAccounts accounts;

    BearerTokenAccounts(UserAccounts accounts) {
        this.accounts = accounts;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt token) {
        String email = token.getClaimAsString(StandardClaimNames.EMAIL);
        if (email == null || email.isBlank()) {
            throw new InvalidBearerTokenException("The access token carries no email");
        }
        String name = DisplayNames.of(
            token.getClaimAsString(StandardClaimNames.NAME),
            token.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME),
            email
        );
        long id = accounts.signIn(Objects.requireNonNull(token.getSubject()), email, name);
        return new AccountTokenAuthentication(id, token);
    }
}
