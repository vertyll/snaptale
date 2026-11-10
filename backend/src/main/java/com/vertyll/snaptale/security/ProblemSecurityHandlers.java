package com.vertyll.snaptale.security;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.vertyll.snaptale.common.MessageKeys;

import tools.jackson.databind.ObjectMapper;

/**
 * Answers the refusals of the security filter chain, which no controller advice sees, with the problem document the
 * rest of the API answers with: {@code errors.status.401} or {@code errors.status.403}. A request that came with a
 * bearer token also gets the challenge RFC 6750 asks for: {@code WWW-Authenticate: Bearer} with
 * {@code error="invalid_token"} when the token was refused, or with {@code error="insufficient_scope"} when the account
 * it names lacks the permission. A browser's session cookie has no scheme to challenge for.
 */
final class ProblemSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final String INSUFFICIENT_SCOPE = "Bearer error=\"insufficient_scope\"";

    private final BearerTokenAuthenticationEntryPoint bearerChallenge = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper objectMapper;

    ProblemSecurityHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        if (exception instanceof OAuth2AuthenticationException) {
            // A bearer token was sent and refused: sets the status and the challenge, and leaves the body to us.
            bearerChallenge.commence(request, response, exception);
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
        writeProblem(response);
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException exception
    ) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        if (request.getUserPrincipal() instanceof AccountTokenAuthentication) {
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, INSUFFICIENT_SCOPE);
        }
        writeProblem(response);
    }

    private void writeProblem(HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatus(response.getStatus());
        problem.setProperty("code", MessageKeys.HTTP_STATUS_PREFIX + response.getStatus());
        problem.setProperty("args", Map.of());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
