package com.vertyll.snaptale.keycloak;

import java.util.Set;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.vertyll.snaptale.common.Language;

import lombok.RequiredArgsConstructor;

/** The way into the person's Keycloak account for the rest of SnapTale. */
@Component
@RequiredArgsConstructor
public final class KeycloakAccounts {

    private static final Set<String> ACCOUNT_ACTIONS = Set.of("UPDATE_PASSWORD", "CONFIGURE_TOTP");

    private static final Pattern DELETE_CREDENTIAL =
            Pattern.compile("delete_credential:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private final KeycloakAccount account;

    /**
     * Whether a sign-in may start this Keycloak action for a signed-in person: changing the password, setting up the
     * second factor, or removing a credential named by its id, which Keycloak asks to confirm and removes only when it
     * is the person's own.
     */
    public static boolean isAccountAction(@Nullable String action) {
        return action != null && (ACCOUNT_ACTIONS.contains(action) || DELETE_CREDENTIAL.matcher(action).matches());
    }

    /** Stores the language on the account, which Keycloak's pages show a signed-in person in. */
    public void keepLanguage(Jwt accessToken, Language language) {
        account.keepLanguage(accessToken, language);
    }
}
