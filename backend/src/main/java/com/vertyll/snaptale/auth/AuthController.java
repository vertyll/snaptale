package com.vertyll.snaptale.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.snaptale.common.MessageKeys;
import com.vertyll.snaptale.common.UnauthorizedException;
import com.vertyll.snaptale.security.CurrentUser;
import com.vertyll.snaptale.security.SessionLogin;
import com.vertyll.snaptale.user.Emails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final AuthService service;
    private final SessionLogin sessionLogin;
    private final LoginThrottle throttle;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        sessionLogin.signIn(service.register(request), httpRequest, httpResponse);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        String email = Emails.normalize(request.email());
        throttle.requireAllowed(email);
        try {
            sessionLogin.signInWithPassword(email, request.password(), httpRequest, httpResponse);
        } catch (AuthenticationException e) {
            throttle.failed(email);
            throw new UnauthorizedException(MessageKeys.AUTH_BAD_CREDENTIALS, e);
        }
        throttle.succeeded(email);
    }

    @PostMapping("/email/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        service.verifyEmail(request);
    }

    @PostMapping("/email/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void resendVerification(CurrentUser user) {
        service.resendVerification(user.id());
    }

    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        service.forgotPassword(request);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        service.resetPassword(request);
    }
}
