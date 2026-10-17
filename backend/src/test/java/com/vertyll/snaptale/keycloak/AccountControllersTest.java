package com.vertyll.snaptale.keycloak;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.vertyll.snaptale.common.Language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountControllersTest {
    private static final Jwt ACCESS_TOKEN =
            Jwt.withTokenValue("access").header("alg", "none").claim("sub", "person").build();

    private final KeycloakAccount account = mock(KeycloakAccount.class);

    @Test
    void aSecondFactorIsAnsweredWithTheCredentialKeycloakRemoves() {
        when(account.secondFactor(ACCESS_TOKEN)).thenReturn(Optional.of("otp-1"));

        SecondFactorController.SecondFactorResponse response =
                new SecondFactorController(account).secondFactor(new JwtAuthenticationToken(ACCESS_TOKEN));

        assertThat(response).isEqualTo(new SecondFactorController.SecondFactorResponse(true, "otp-1"));
    }

    @Test
    void noSecondFactorIsAnsweredAsOff() {
        when(account.secondFactor(ACCESS_TOKEN)).thenReturn(Optional.empty());

        SecondFactorController.SecondFactorResponse response =
                new SecondFactorController(account).secondFactor(new JwtAuthenticationToken(ACCESS_TOKEN));

        assertThat(response).isEqualTo(new SecondFactorController.SecondFactorResponse(false, null));
    }

    @Test
    void theLanguageChosenIsKeptOnTheCallersAccount() {
        new AccountLanguageController(account).keepLanguage(
            new JwtAuthenticationToken(ACCESS_TOKEN),
            new AccountLanguageController.LanguageRequest(Language.EN)
        );

        verify(account).keepLanguage(ACCESS_TOKEN, Language.EN);
    }
}
