package com.vertyll.snaptale.keycloak;

import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Where Keycloak is and how SnapTale signs in to it, for the security set-up and for the account API alike. */
@Validated
@ConfigurationProperties("application.keycloak")
public record KeycloakProperties(
    @NotBlank String realmUrl,
    @Nullable String backchannelRealmUrl,
    @NotBlank String clientId,
    @NotBlank String clientSecret,
    @NotBlank String audience
) {

    /** The realm's address as SnapTale reaches it from inside the cluster, which may differ from the browser's. */
    public String backchannel() {
        return backchannelRealmUrl == null || backchannelRealmUrl.isBlank() ? realmUrl : backchannelRealmUrl;
    }
}
