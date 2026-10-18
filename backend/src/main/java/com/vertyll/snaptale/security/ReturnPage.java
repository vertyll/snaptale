package com.vertyll.snaptale.security;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.jspecify.annotations.Nullable;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;

/**
 * The page a sign-in returns to, named by the front-end in {@code return_to}. It is kept as Spring Security's saved
 * request, so Spring's own success handler returns there; only a path on the front-end's own site is kept, so the sign-in cannot
 * send the browser elsewhere.
 */
final class ReturnPage {

    static final String PARAMETER = "return_to";

    private static final String ATTRIBUTE = ReturnPage.class.getName();

    private static final int MOST_LENGTH = 2048;

    private ReturnPage() {
    }

    static RequestCache requestCache() {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        cache.setSessionAttrName(ATTRIBUTE);
        return cache;
    }

    /**
     * Saves the page this sign-in names as the request it returns to. A sign-in that names none forgets a page an
     * earlier one named, and leaves a request Spring Security saved itself.
     */
    static void remember(HttpServletRequest request) {
        Optional<String> page = onThisSite(request.getParameter(PARAMETER));
        if (page.isPresent()) {
            request.getSession().setAttribute(ATTRIBUTE, new SimpleSavedRequest(page.get()));
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(ATTRIBUTE) instanceof SimpleSavedRequest) {
            session.removeAttribute(ATTRIBUTE);
        }
    }

    static Optional<String> onThisSite(@Nullable String path) {
        return Optional.ofNullable(path)
            .filter(
                candidate -> candidate.startsWith("/") && !candidate.startsWith("//") && candidate.indexOf('\\') < 0
                        && candidate.chars().noneMatch(Character::isISOControl) && candidate.length() <= MOST_LENGTH
            );
    }
}
