package com.vertyll.snaptale.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

final class SessionAccessTokenFilter extends OncePerRequestFilter {
    private static final RequestMatcher API = PathPatternRequestMatcher.withDefaults().matcher("/api/**");

    private final SessionAccessTokens accessTokens;

    SessionAccessTokenFilter(SessionAccessTokens accessTokens) {
        super();
        this.accessTokens = accessTokens;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !API.matches(request);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current instanceof OAuth2AuthenticationToken session) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            accessTokens.current(session, request, response)
                .ifPresent(token -> context.setAuthentication(new AccessTokenAuthentication(session, token)));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }
}
