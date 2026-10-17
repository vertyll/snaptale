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
        String username = token.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
        if (username == null || username.isBlank()) {
            throw new InvalidBearerTokenException("The access token carries no username");
        }
        long id = accounts.signIn(Objects.requireNonNull(token.getSubject()), email, username);
        return new AccountTokenAuthentication(id, token);
    }
}
