package com.vertyll.snaptale.keycloak;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import com.vertyll.snaptale.common.Language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KeycloakAccountsTest {

    @ParameterizedTest
    @ValueSource(
        strings = {
            "UPDATE_PASSWORD",
            "CONFIGURE_TOTP",
            "delete_credential:0b7f6a52-3c1e-4d2a-9f5b-6e8d7c1a2b3c"
        }
    )
    void anAccountActionIsPassedOnToKeycloak(String action) {
        Map<String, Object> parameters = new HashMap<>();

        KeycloakAccounts.passOnAccountAction(withAction(action), parameters);

        assertThat(parameters).containsExactly(Map.entry("kc_action", action));
        assertThat(KeycloakAccounts.asksForAccountAction(withAction(action))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "delete_account",
            "delete_credential",
            "delete_credential:otp",
            "delete_credential:0B7F6A52-3C1E-4D2A-9F5B-6E8D7C1A2B3C",
            "delete_credential:0b7f6a52-3c1e-4d2a-9f5b-6e8d7c1a2b3c&prompt=none",
            "VERIFY_EMAIL"
        }
    )
    void anActionKeycloakIsNotAskedForIsLeftOut(String action) {
        Map<String, Object> parameters = new HashMap<>();

        KeycloakAccounts.passOnAccountAction(withAction(action), parameters);

        assertThat(parameters).isEmpty();
        assertThat(KeycloakAccounts.asksForAccountAction(withAction(action))).isFalse();
    }

    @Test
    void aSignInWithoutAnActionAsksForNone() {
        assertThat(KeycloakAccounts.asksForAccountAction(new MockHttpServletRequest())).isFalse();
    }

    @Test
    void theLanguageIsKeptOnTheAccountOfTheToken() {
        KeycloakAccount account = mock(KeycloakAccount.class);
        Jwt accessToken = Jwt.withTokenValue("access").header("alg", "none").claim("sub", "person").build();

        new KeycloakAccounts(account).keepLanguage(accessToken, Language.PL);

        verify(account).keepLanguage(accessToken, Language.PL);
    }

    private static MockHttpServletRequest withAction(String action) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("kc_action", action);
        return request;
    }
}
