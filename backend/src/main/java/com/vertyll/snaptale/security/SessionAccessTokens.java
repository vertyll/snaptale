package com.vertyll.snaptale.security;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class SessionAccessTokens {

    private final OAuth2AuthorizedClientManager authorizedClients;
    private final JwtDecoder accessTokens;

    Optional<Jwt> current(OAuth2AuthenticationToken session, HttpServletRequest request, HttpServletResponse response) {
        try {
            OAuth2AuthorizedClient client = authorizedClients.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(session.getAuthorizedClientRegistrationId())
                    .principal(session)
                    .attribute(HttpServletRequest.class.getName(), request)
                    .attribute(HttpServletResponse.class.getName(), response)
                    .build()
            );
            return client == null ? Optional.empty()
                    : Optional.of(accessTokens.decode(client.getAccessToken().getTokenValue()));
        } catch (ClientAuthorizationException e) {
            if (OAuth2ErrorCodes.INVALID_GRANT.equals(e.getError().getErrorCode())) {
                HttpSession httpSession = request.getSession(false);
                if (httpSession != null) {
                    httpSession.invalidate();
                }
                log.info("Session of {} ended: Keycloak refused the refresh", session.getName());
            } else {
                log.warn("Could not refresh the session of {}: {}", session.getName(), e.getError().getErrorCode());
            }
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("Access token of {} rejected: {}", session.getName(), e.getMessage());
            return Optional.empty();
        }
    }
}
