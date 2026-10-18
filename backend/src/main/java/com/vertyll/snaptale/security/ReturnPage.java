package com.vertyll.snaptale.security;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.jspecify.annotations.Nullable;

/**
 * The page a sign-in returns to, named by the front-end in {@code return_to} and kept in the session while Keycloak
 * signs the person in. Only a path on the front-end's own site is kept, so the sign-in cannot send the browser
 * elsewhere.
 */
final class ReturnPage {

    static final String PARAMETER = "return_to";

    private static final String ATTRIBUTE = ReturnPage.class.getName();

    private static final int MOST_LENGTH = 2048;

    private ReturnPage() {
    }

    /** Keeps the page this sign-in names, or forgets an earlier one when it names none on this site. */
    static void remember(HttpServletRequest request) {
        Optional<String> page = onThisSite(request.getParameter(PARAMETER));
        if (page.isPresent()) {
            request.getSession().setAttribute(ATTRIBUTE, page.get());
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ATTRIBUTE);
        }
    }

    /** The page the completed sign-in returns to, once. */
    static Optional<String> take(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object page = session.getAttribute(ATTRIBUTE);
        session.removeAttribute(ATTRIBUTE);
        return page instanceof String path ? onThisSite(path) : Optional.empty();
    }

    /** A path that starts with a single {@code /}, with no backslash and no control character. */
    static Optional<String> onThisSite(@Nullable String path) {
        return Optional.ofNullable(path)
            .filter(
                candidate -> candidate.startsWith("/") && !candidate.startsWith("//") && candidate.indexOf('\\') < 0
                        && candidate.chars().noneMatch(Character::isISOControl) && candidate.length() <= MOST_LENGTH
            );
    }
}
