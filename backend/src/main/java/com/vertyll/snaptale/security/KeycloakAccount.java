package com.vertyll.snaptale.security;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.vertyll.snaptale.common.Language;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Keycloak's account API, called with the signed-in person's own access token: it answers about that person alone, so
 * SnapTale needs no right to read anyone else's credentials.
 */
@Slf4j
@Component
class KeycloakAccount {

    private static final String LOCALE_ATTRIBUTE = "locale";

    private static final ParameterizedTypeReference<Map<String, Object>> ACCOUNT = new ParameterizedTypeReference<>() {
    };

    private static final String OTP_CREDENTIAL = "otp";

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final RestClient keycloak;

    @Autowired
    KeycloakAccount(KeycloakProperties properties) {
        this(RestClient.builder().baseUrl(properties.backchannel()).requestFactory(requestFactory()));
    }

    KeycloakAccount(RestClient.Builder keycloak) {
        this.keycloak = keycloak.build();
    }

    /** The id of the person's one-time-password credential, which Keycloak's {@code delete_credential} action takes. */
    Optional<String> secondFactor(Jwt accessToken) {
        CredentialType[] types = keycloak.get()
            .uri("/account/credentials?type={type}", OTP_CREDENTIAL)
            .headers(headers -> headers.setBearerAuth(accessToken.getTokenValue()))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(CredentialType[].class);
        return Arrays.stream(types == null ? new CredentialType[0] : types)
            .filter(type -> OTP_CREDENTIAL.equals(type.type()))
            .flatMap(type -> type.credentials().stream())
            .map(metadata -> metadata.credential().id())
            .findFirst();
    }

    /**
     * Stores the language on the person's account, which Keycloak's pages show a signed-in person in, over the language
     * SnapTale asks for. The account is read and written back whole, so nothing else on it changes; a failure leaves
     * Keycloak's pages in the language they had, and is only logged.
     */
    void keepLanguage(Jwt accessToken, Language language) {
        try {
            Map<String, Object> account = keycloak.get()
                .uri("/account/")
                .headers(headers -> headers.setBearerAuth(accessToken.getTokenValue()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ACCOUNT);
            if (account == null) {
                return;
            }
            Map<String, Object> attributes = new LinkedHashMap<>();
            if (account.get("attributes") instanceof Map<?, ?> stored) {
                stored.forEach((name, value) -> attributes.put(String.valueOf(name), value));
            }
            List<String> locale = List.of(language.tag());
            if (locale.equals(attributes.get(LOCALE_ATTRIBUTE))) {
                return;
            }
            attributes.put(LOCALE_ATTRIBUTE, locale);
            account.put("attributes", attributes);
            keycloak.post()
                .uri("/account/")
                .headers(headers -> headers.setBearerAuth(accessToken.getTokenValue()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(account)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Could not store the account's language in Keycloak: {}", e.getMessage());
        }
    }

    private static JdkClientHttpRequestFactory requestFactory() {
        JdkClientHttpRequestFactory requests =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        requests.setReadTimeout(TIMEOUT);
        return requests;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CredentialType(String type, @Nullable List<CredentialMetadata> userCredentialMetadatas) {

        List<CredentialMetadata> credentials() {
            return userCredentialMetadatas == null ? List.of() : userCredentialMetadatas;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CredentialMetadata(Credential credential) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Credential(String id) {
    }
}
