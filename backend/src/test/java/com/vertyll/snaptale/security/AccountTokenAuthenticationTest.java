package com.vertyll.snaptale.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTokenAuthenticationTest {

    @Test
    void twoAuthenticationsOfOneAccountWithOneAccessTokenAreEqual() {
        Jwt accessToken = accessToken("first");

        AccountTokenAuthentication one = new AccountTokenAuthentication(7, accessToken);
        AccountTokenAuthentication other = new AccountTokenAuthentication(7, accessToken);

        assertThat(one).isEqualTo(other).hasSameHashCodeAs(other);
    }

    @Test
    void authenticationsOfDifferentAccountsAreNotEqual() {
        Jwt accessToken = accessToken("first");

        AccountTokenAuthentication one = new AccountTokenAuthentication(7, accessToken);
        AccountTokenAuthentication other = new AccountTokenAuthentication(8, accessToken);

        assertThat(one).isNotEqualTo(other);
    }

    @Test
    void authenticationsWithDifferentAccessTokensAreNotEqual() {
        AccountTokenAuthentication one = new AccountTokenAuthentication(7, accessToken("first"));
        AccountTokenAuthentication other = new AccountTokenAuthentication(7, accessToken("second"));

        assertThat(one).isNotEqualTo(other);
    }

    private static Jwt accessToken(String value) {
        return Jwt.withTokenValue(value).header("alg", "RS256").subject("ada").build();
    }
}
