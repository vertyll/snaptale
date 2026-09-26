package com.vertyll.snaptale.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class AccountMailer {

    private static final Locale POLISH = Locale.forLanguageTag("pl");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AuthProperties properties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void send(AccountMailRequested request) {
        AccountMailRequested.Kind kind = request.kind();
        String link = properties.frontendUrl() + kind.frontendPath() + "?token="
                + URLEncoder.encode(request.token(), StandardCharsets.UTF_8);
        Context context = new Context(POLISH);
        context.setVariable("name", request.name());
        context.setVariable("link", link);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(
                new InternetAddress(properties.mailFrom(), properties.mailFromName(), StandardCharsets.UTF_8.name())
            );
            helper.setTo(request.email());
            helper.setSubject(kind.subject());
            helper.setText(templateEngine.process(kind.template(), context), true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException | MailException | TemplateProcessingException e) {
            log.error("[ERROR] Failed to send the {} e-mail to {}", kind, request.email(), e);
        }
    }
}
