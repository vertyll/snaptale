package com.vertyll.snaptale.auth;

import org.jspecify.annotations.Nullable;

final class DisplayNames {

    private DisplayNames() {
    }

    static String of(@Nullable String fullName, @Nullable String preferredUsername, String email) {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return preferredUsername != null && !preferredUsername.isBlank() ? preferredUsername
                : email.substring(0, email.indexOf('@'));
    }
}
