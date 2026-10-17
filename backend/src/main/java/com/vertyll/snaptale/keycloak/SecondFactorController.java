package com.vertyll.snaptale.keycloak;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Whether the caller set up a second factor, and the credential the account menu's turn-off button names to Keycloak.
 */
@RestController
@RequestMapping("/api/me/second-factor")
@RequiredArgsConstructor
class SecondFactorController {

    private final KeycloakAccount account;

    @GetMapping
    SecondFactorResponse secondFactor(Authentication authentication) {
        if (!(authentication.getCredentials() instanceof Jwt accessToken)) {
            throw new IllegalStateException("An API request is authorized by an access token");
        }
        return account.secondFactor(accessToken)
            .map(SecondFactorResponse::enabled)
            .orElseGet(SecondFactorResponse::disabled);
    }

    record SecondFactorResponse(boolean enabled, @Nullable String credentialId) {

        static SecondFactorResponse enabled(String credentialId) {
            return new SecondFactorResponse(true, credentialId);
        }

        static SecondFactorResponse disabled() {
            return new SecondFactorResponse(false, null);
        }
    }
}
