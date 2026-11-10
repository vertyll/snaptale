package com.vertyll.snaptale.security;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenAuthenticationTest {

    private static final OAuth2User ADA = new DefaultOAuth2User(List.of(), Map.of("sub", "ada"), "sub");
    private static final OAuth2User GRACE = new DefaultOAuth2User(List.of(), Map.of("sub", "grace"), "sub");

    @Test
    void twoAuthenticationsOfOneSessionWithOneAccessTokenAreEqual() {
        OAuth2AuthenticationToken session = session(ADA);
        Jwt accessToken = accessToken("first");

        AccessTokenAuthentication one = new AccessTokenAuthentication(session, accessToken);
        AccessTokenAuthentication other = new AccessTokenAuthentication(session, accessToken);

        assertThat(one).isEqualTo(other).hasSameHashCodeAs(other);
    }

    @Test
    void authenticationsWithDifferentAccessTokensAreNotEqual() {
        OAuth2AuthenticationToken session = session(ADA);

        AccessTokenAuthentication one = new AccessTokenAuthentication(session, accessToken("first"));
        AccessTokenAuthentication other = new AccessTokenAuthentication(session, accessToken("second"));

        assertThat(one).isNotEqualTo(other);
    }

    @Test
    void authenticationsOfDifferentPeopleAreNotEqual() {
        Jwt accessToken = accessToken("first");

        AccessTokenAuthentication one = new AccessTokenAuthentication(session(ADA), accessToken);
        AccessTokenAuthentication other = new AccessTokenAuthentication(session(GRACE), accessToken);

        assertThat(one).isNotEqualTo(other);
    }

    private static OAuth2AuthenticationToken session(OAuth2User user) {
        return new OAuth2AuthenticationToken(user, List.of(), "keycloak");
    }

    private static Jwt accessToken(String value) {
        return Jwt.withTokenValue(value).header("alg", "RS256").subject("ada").build();
    }
}
