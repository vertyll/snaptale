package com.vertyll.snaptale.security;

import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.keycloak")
record KeycloakProperties(
    @NotBlank String realmUrl,
    @Nullable String backchannelRealmUrl,
    @NotBlank String clientId,
    @NotBlank String clientSecret
) {

    String backchannel() {
        return backchannelRealmUrl == null || backchannelRealmUrl.isBlank() ? realmUrl : backchannelRealmUrl;
    }
}
