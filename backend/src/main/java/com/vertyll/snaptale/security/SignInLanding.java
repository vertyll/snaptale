package com.vertyll.snaptale.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * Where a completed sign-in lands: Spring Security's saved request, the page {@link ReturnPage} kept, or SnapTale's
 * start page. The page is used once.
 */
final class SignInLanding extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = ReturnPage.requestCache();

    SignInLanding() {
        super();
        setRequestCache(requestCache);
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws ServletException, IOException {
        super.onAuthenticationSuccess(request, response, authentication);
        requestCache.removeRequest(request, response);
    }
}
