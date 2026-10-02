package com.vertyll.snaptale;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.vertyll.snaptale.security.AuthenticatedUser;
import com.vertyll.snaptale.user.UserAccounts;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

@Component
public class TestUsers {

    private final UserAccounts accounts;

    TestUsers(UserAccounts accounts) {
        this.accounts = accounts;
    }

    public TestUser create(String name) {
        String keycloakId = UUID.randomUUID().toString();
        String email = name.toLowerCase(Locale.ROOT).replace(' ', '.') + "-" + keycloakId + "@example.com";
        return new TestUser(accounts.signIn(keycloakId, email, name), keycloakId, email);
    }

    public static RequestPostProcessor as(TestUser user) {
        return oidcLogin().oidcUser(principal(user.id(), user.keycloakId(), user.email()));
    }

    public static AuthenticatedUser principal(long id, String keycloakId, String email) {
        Instant now = Instant.now();
        OidcIdToken token = OidcIdToken.withTokenValue("test-id-token")
            .subject(keycloakId)
            .claim(StandardClaimNames.EMAIL, email)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .build();
        return new AuthenticatedUser(id, new DefaultOidcUser(List.of(), token));
    }

    public record TestUser(long id, String keycloakId, String email) {
    }
}
