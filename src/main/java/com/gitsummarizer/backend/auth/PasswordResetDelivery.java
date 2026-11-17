package com.gitsummarizer.backend.auth;

import com.gitsummarizer.backend.user.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class PasswordResetDelivery {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetDelivery.class);
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String frontendUrl;
    private final String from;
    private final boolean exposeLink;

    public PasswordResetDelivery(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${auth.reset.frontend-url:http://localhost:3000}") String frontendUrl,
            @Value("${auth.reset.from:noreply@gitsummarizer.local}") String from,
            @Value("${auth.reset.expose-link:false}") boolean exposeLink) {
        this.mailSenderProvider = mailSenderProvider;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.from = from;
        this.exposeLink = exposeLink;
    }

    /** Returns a reset URL only when local development has explicitly enabled it. */
    public String deliver(AppUser user, String token) {
        String link = frontendUrl + "/reset-password?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(user.getEmail());
                message.setSubject("Reset your GitSummarizer password");
                message.setText("Use this link within 30 minutes to reset your password:\n\n" + link);
                sender.send(message);
            } catch (RuntimeException exception) {
                log.warn("Could not send a password reset email.", exception);
            }
        }
        if (exposeLink) return link;
        if (sender == null) log.info("Password reset requested. Configure SMTP to deliver reset links.");
        return null;
    }
}
