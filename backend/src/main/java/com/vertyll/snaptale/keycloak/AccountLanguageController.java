package com.vertyll.snaptale.keycloak;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.snaptale.common.Language;
import com.vertyll.snaptale.common.MessageKeys;

import lombok.RequiredArgsConstructor;

/**
 * Stores the language chosen in SnapTale on the caller's Keycloak account, which Keycloak's pages show a signed-in
 * person in, so they open in SnapTale's language however the person reaches them.
 */
@RestController
@RequestMapping("/api/me/language")
@RequiredArgsConstructor
class AccountLanguageController {

    private final KeycloakAccount account;

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void keepLanguage(Authentication authentication, @Valid @RequestBody LanguageRequest request) {
        if (!(authentication.getCredentials() instanceof Jwt accessToken)) {
            throw new IllegalStateException("An API request is authorized by an access token");
        }
        account.keepLanguage(accessToken, request.language());
    }

    record LanguageRequest(@NotNull(message = MessageKeys.REQUIRED) Language language) {
    }
}
