package com.vertyll.snaptale.auth;

record AccountMailRequested(Kind kind, String email, String name, String token) {

    enum Kind {
        EMAIL_VERIFICATION("email/email-verification", "Potwierdź adres e-mail w SnapTale", "/verify-email"),
        PASSWORD_RESET("email/password-reset", "Reset hasła w SnapTale", "/reset-password");

        private final String template;
        private final String subject;
        private final String frontendPath;

        Kind(String template, String subject, String frontendPath) {
            this.template = template;
            this.subject = subject;
            this.frontendPath = frontendPath;
        }

        String template() {
            return template;
        }

        String subject() {
            return subject;
        }

        String frontendPath() {
            return frontendPath;
        }
    }
}
