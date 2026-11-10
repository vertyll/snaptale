package com.vertyll.snaptale.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionStoreConfigTest {

    private static final String REGISTRATION_ID = "keycloak";
    private static final Instant ISSUED = Instant.parse("2026-11-10T08:00:00Z");
    private static final Instant EXPIRES = ISSUED.plusSeconds(300);

    private final RedisSerializer<Object> serializer = SessionStoreConfig.sessionSerializer();

    @Test
    void theAuthorizedClientWithItsTokensSurvivesARoundTripThroughTheStore() {
        Map<String, OAuth2AuthorizedClient> clients = new HashMap<>();
        clients.put(REGISTRATION_ID, authorizedClient());

        byte[] stored = serializer.serialize(clients);

        assertThat(stored[0]).isEqualTo((byte) '{');
        Map<?, ?> read = roundTrip(clients, Map.class);
        OAuth2AuthorizedClient client = (OAuth2AuthorizedClient) Objects.requireNonNull(read.get(REGISTRATION_ID));
        assertThat(client.getPrincipalName()).isEqualTo("ada");
        assertThat(client.getClientRegistration().getRegistrationId()).isEqualTo(REGISTRATION_ID);
        assertThat(client.getAccessToken().getTokenValue()).isEqualTo("access-token");
        assertThat(client.getAccessToken().getExpiresAt()).isEqualTo(EXPIRES);
        assertThat(client.getAccessToken().getScopes()).containsExactlyInAnyOrder("openid", "email");
        assertThat(Objects.requireNonNull(client.getRefreshToken()).getTokenValue()).isEqualTo("refresh-token");
    }

    @Test
    void theSignedInUserWithItsIdSurvivesARoundTripThroughTheStore() {
        OidcIdToken idToken = new OidcIdToken("id-token", ISSUED, EXPIRES, Map.of("sub", "ada", "iss", "https://kc"));
        AuthenticatedUser user = new AuthenticatedUser(
            42,
            new DefaultOidcUser(List.of(new SimpleGrantedAuthority("OIDC_USER")), idToken)
        );
        SecurityContextImpl context =
                new SecurityContextImpl(new OAuth2AuthenticationToken(user, user.getAuthorities(), REGISTRATION_ID));

        SecurityContextImpl read = roundTrip(context, SecurityContextImpl.class);

        OAuth2AuthenticationToken authentication =
                (OAuth2AuthenticationToken) Objects.requireNonNull(read.getAuthentication());
        assertThat(authentication.getAuthorizedClientRegistrationId()).isEqualTo(REGISTRATION_ID);
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(user);
        AuthenticatedUser readUser = (AuthenticatedUser) Objects.requireNonNull(authentication.getPrincipal());
        assertThat(readUser.id()).isEqualTo(42);
        assertThat(readUser.getName()).isEqualTo("ada");
    }

    @Test
    void aSignInInProgressSurvivesARoundTripThroughTheStore() {
        OAuth2AuthorizationRequest request = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://kc/auth")
            .clientId("snaptale-backend")
            .redirectUri("http://localhost:3000/login/oauth2/code/keycloak")
            .scopes(Set.of("openid"))
            .state("state")
            .build();

        OAuth2AuthorizationRequest read = roundTrip(request, OAuth2AuthorizationRequest.class);

        assertThat(read.getState()).isEqualTo("state");
        assertThat(read.getRedirectUri()).isEqualTo("http://localhost:3000/login/oauth2/code/keycloak");
    }

    @Test
    void thePageASignInReturnsToSurvivesARoundTripThroughTheStore() {
        SimpleSavedRequest read = roundTrip(new SimpleSavedRequest("/post/7?comments=open"), SimpleSavedRequest.class);

        assertThat(read.getRedirectUrl()).isEqualTo("/post/7?comments=open");
        assertThat(read.getMethod()).isEqualTo("GET");
    }

    @Test
    void aStoredValueNamingAClassOutsideTheAllowedOnesIsRefused() {
        byte[] stored = "{\"@class\":\"java.lang.ProcessBuilder\",\"command\":[\"true\"]}".getBytes(UTF_8);

        assertThatThrownBy(() -> serializer.deserialize(stored)).isInstanceOf(SerializationException.class);
    }

    private <T> T roundTrip(Object value, Class<T> type) {
        return type.cast(Objects.requireNonNull(serializer.deserialize(serializer.serialize(value))));
    }

    private static OAuth2AuthorizedClient authorizedClient() {
        ClientRegistration registration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientId("snaptale-backend")
            .clientSecret("secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost:3000/login/oauth2/code/{registrationId}")
            .scope("openid", "email")
            .authorizationUri("https://kc/auth")
            .tokenUri("https://kc/token")
            .jwkSetUri("https://kc/certs")
            .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "access-token",
            ISSUED,
            EXPIRES,
            Set.of("openid", "email")
        );
        return new OAuth2AuthorizedClient(
            registration,
            "ada",
            accessToken,
            new OAuth2RefreshToken("refresh-token", ISSUED)
        );
    }
}
