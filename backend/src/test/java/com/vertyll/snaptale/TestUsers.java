package com.vertyll.snaptale;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.vertyll.snaptale.security.AuthenticatedUser;
import com.vertyll.snaptale.user.UserAccounts;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

@Component
public class TestUsers {

    private static final String REGISTRATION_ID = "keycloak";

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
        return signedIn(user, TestAccessTokens.of(user.keycloakId()));
    }

    public static RequestPostProcessor withRejectedAccessToken(TestUser user) {
        return signedIn(user, TestAccessTokens.REJECTED);
    }

    private static RequestPostProcessor signedIn(TestUser user, String accessToken) {
        Instant now = Instant.now();
        ClientRegistration registration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientId("test-client")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost/login/oauth2/code/{registrationId}")
            .authorizationUri("http://localhost/auth")
            .tokenUri("http://localhost/token")
            .build();
        RequestPostProcessor login = oidcLogin().clientRegistration(registration)
            .oidcUser(principal(user.id(), user.keycloakId(), user.email()));
        RequestPostProcessor client = oauth2Client(REGISTRATION_ID).clientRegistration(registration)
            .principalName(user.keycloakId())
            .accessToken(
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, accessToken, now, now.plusSeconds(300))
            );
        return request -> client.postProcessRequest(login.postProcessRequest(request));
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
