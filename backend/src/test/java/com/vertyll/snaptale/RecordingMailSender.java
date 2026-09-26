package com.vertyll.snaptale;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSenderImpl;

public class RecordingMailSender extends JavaMailSenderImpl {

    private static final Pattern TOKEN = Pattern.compile("token=([A-Za-z0-9_-]+)");

    private final List<Sent> sent = new CopyOnWriteArrayList<>();

    @Override
    public void send(MimeMessage... messages) {
        for (MimeMessage message : messages) {
            try {
                String to = ((InternetAddress) message.getAllRecipients()[0]).getAddress();
                sent.add(new Sent(to, message.getSubject(), String.valueOf(message.getContent())));
            } catch (MessagingException | IOException e) {
                throw new IllegalStateException("Unreadable test e-mail", e);
            }
        }
    }

    public List<Sent> sentTo(String email) {
        return sent.stream().filter(mail -> mail.to().equals(email)).toList();
    }

    public String lastTokenFor(String email) {
        List<Sent> mails = sentTo(email);
        if (mails.isEmpty()) {
            throw new IllegalStateException("No e-mail was sent to " + email);
        }
        Matcher matcher = TOKEN.matcher(mails.getLast().body());
        if (!matcher.find()) {
            throw new IllegalStateException("No token in the e-mail to " + email);
        }
        return matcher.group(1);
    }

    public record Sent(String to, String subject, String body) {
    }
}
