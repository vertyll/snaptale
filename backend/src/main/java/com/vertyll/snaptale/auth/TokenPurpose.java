package com.vertyll.snaptale.auth;

import java.time.Duration;

enum TokenPurpose {
    PASSWORD_RESET(Duration.ofHours(1)),
    EMAIL_VERIFICATION(Duration.ofDays(2));

    private final Duration validity;

    TokenPurpose(Duration validity) {
        this.validity = validity;
    }

    Duration validity() {
        return validity;
    }
}
