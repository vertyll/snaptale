package com.vertyll.snaptale.security;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import tools.jackson.databind.ObjectMapper;

final class LogoutUrlResponder extends OidcClientInitiatedLogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    LogoutUrlResponder(ClientRegistrationRepository registrations, String frontendUrl, ObjectMapper objectMapper) {
        super(registrations);
        this.objectMapper = objectMapper;
        setPostLogoutRedirectUri(frontendUrl + "/");
        setDefaultTargetUrl("/");
    }

    @Override
    public void onLogoutSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        @Nullable Authentication authentication
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
            response.getOutputStream(),
            Map.of("logoutUrl", determineTargetUrl(request, response, authentication))
        );
    }
}
