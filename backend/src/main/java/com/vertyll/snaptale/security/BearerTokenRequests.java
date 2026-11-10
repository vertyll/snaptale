package com.vertyll.snaptale.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Requests that carry a bearer token, which need no CSRF token.
 *
 * <p>
 * CSRF forges a request the browser authenticates by itself, with the session cookie it sends to this site. A browser
 * never adds an {@code Authorization} header on its own, and a page of another site cannot set one without a CORS
 * preflight this application does not allow, so a request authenticated by a bearer token cannot be forged. The token
 * is found as the resource server finds it, so exactly the requests it authenticates are matched: a request with
 * another scheme is still authenticated by its session cookie, and still needs the CSRF token. A malformed bearer token
 * matches too, as the resource server refuses it with {@code 401}.
 */
final class BearerTokenRequests implements RequestMatcher {

    private final BearerTokenResolver bearerTokens = new DefaultBearerTokenResolver();

    @Override
    public boolean matches(HttpServletRequest request) {
        try {
            return bearerTokens.resolve(request) != null;
        } catch (OAuth2AuthenticationException malformed) {
            return true;
        }
    }
}
