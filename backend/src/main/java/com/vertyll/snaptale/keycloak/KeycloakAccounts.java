package com.vertyll.snaptale.keycloak;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.vertyll.snaptale.common.Language;

import lombok.RequiredArgsConstructor;

/** The way into the person's Keycloak account for the rest of SnapTale. */
@Component
@RequiredArgsConstructor
public final class KeycloakAccounts {

    /** Keycloak's parameter naming the one page a sign-in shows a signed-in person before returning. */
    private static final String ACCOUNT_ACTION_PARAMETER = "kc_action";

    private static final Set<String> ACCOUNT_ACTIONS = Set.of("UPDATE_PASSWORD", "CONFIGURE_TOTP");

    private static final Pattern DELETE_CREDENTIAL =
            Pattern.compile("delete_credential:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private final KeycloakAccount account;

    /** Whether this request asks a sign-in to start an account action a signed-in person may start. */
    public static boolean asksForAccountAction(HttpServletRequest request) {
        return accountAction(request).isPresent();
    }

    /**
     * Asks Keycloak, among an authorization request's parameters, to show only the page of the account action this
     * request names, when it is one a signed-in person may start. Any other value is left out, so no other page can be
     * reached.
     */
    public static void passOnAccountAction(HttpServletRequest request, Map<String, Object> parameters) {
        accountAction(request).ifPresent(action -> parameters.put(ACCOUNT_ACTION_PARAMETER, action));
    }

    /**
     * Changing the password, setting up the second factor, or removing a credential named by its id, which Keycloak
     * asks to confirm and removes only when it is the person's own.
     */
    private static Optional<String> accountAction(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(ACCOUNT_ACTION_PARAMETER))
            .filter(action -> ACCOUNT_ACTIONS.contains(action) || DELETE_CREDENTIAL.matcher(action).matches());
    }

    /** Stores the language on the account, which Keycloak's pages show a signed-in person in. */
    public void keepLanguage(Jwt accessToken, Language language) {
        account.keepLanguage(accessToken, language);
    }
}
