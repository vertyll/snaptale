package com.vertyll.snaptale.security;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Keycloak's account API, called with the signed-in person's own access token: it answers about that person alone, so
 * SnapTale needs no right to read anyone else's credentials.
 */
@Component
class KeycloakAccount {

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
