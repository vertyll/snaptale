package com.vertyll.snaptale.auth;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.snaptale.common.InvalidRequestException;
import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.ValidationLimits;
import com.vertyll.snaptale.security.AuthenticatedUser;
import com.vertyll.snaptale.user.Account;
import com.vertyll.snaptale.user.UserAccounts;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class AuthService {

    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserAccounts accounts;
    private final UserTokens tokens;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    @Transactional
    AuthenticatedUser register(RegisterRequest request) {
        Account account = accounts.register(request.name(), request.email(), encode(request.password()));
        requestVerification(account);
        return new AuthenticatedUser(account.id(), account.email(), null);
    }

    @Transactional
    void resendVerification(long userId) {
        Account account = accounts.get(userId);
        if (!account.emailVerified()) {
            requestVerification(account);
        }
    }

    @Transactional
    void verifyEmail(VerifyEmailRequest request) {
        accounts.markEmailVerified(tokens.consume(request.token(), TokenPurpose.EMAIL_VERIFICATION));
    }

    @Transactional
    void forgotPassword(ForgotPasswordRequest request) {
        accounts.findByEmail(request.email())
            .ifPresent(
                account -> events.publishEvent(
                    new AccountMailRequested(
                        AccountMailRequested.Kind.PASSWORD_RESET,
                        account.email(),
                        account.name(),
                        tokens.issue(account.id(), TokenPurpose.PASSWORD_RESET)
                    )
                )
            );
    }

    @Transactional
    void resetPassword(ResetPasswordRequest request) {
        long userId = tokens.consume(request.token(), TokenPurpose.PASSWORD_RESET);
        accounts.changePassword(userId, encode(request.password()));
    }

    private void requestVerification(Account account) {
        events.publishEvent(
            new AccountMailRequested(
                AccountMailRequested.Kind.EMAIL_VERIFICATION,
                account.email(),
                account.name(),
                tokens.issue(account.id(), TokenPurpose.EMAIL_VERIFICATION)
            )
        );
    }

    private String encode(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidRequestException(
                MessageKeys.TOO_LONG,
                Map.of("max", ValidationLimits.PASSWORD_MAX_LENGTH)
            );
        }
        return passwordEncoder.encode(password);
    }
}
