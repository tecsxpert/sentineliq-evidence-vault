package com.internship.tool.service;

import com.internship.tool.entity.Evidence;
import com.internship.tool.entity.User;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailNotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final TemplateEngine templateEngine;
    private final String fromAddress;
    private final String mailHost;

    public EmailNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                    TemplateEngine templateEngine,
                                    @Value("${app.mail.from:no-reply@evidence-vault.local}") String fromAddress,
                                    @Value("${spring.mail.host:}") String mailHost) {
        this.mailSenderProvider = mailSenderProvider;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
        this.mailHost = mailHost;
    }

    public boolean sendDeadlineReminder(Evidence evidence) {
        if (evidence.getUser() == null || evidence.getUser().getUsername() == null) {
            return false;
        }

        String recipient = evidence.getUser().getUsername();
        if (!recipient.contains("@") || mailHost == null || mailHost.isBlank()) {
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return false;
        }

        try {
            Context context = new Context();
            context.setVariable("evidence", evidence);
            String body = templateEngine.process("deadline-reminder", context);

            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromAddress);
            helper.setTo(recipient);
            helper.setSubject("Evidence deadline reminder: " + evidence.getName());
            helper.setText(body, true);

            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean sendWelcomeEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }

        Context context = new Context();
        context.setVariable("username", user.getUsername());
        String body = templateEngine.process("welcome-email", context);

        return sendHtml(user.getEmail(), "Welcome to Evidence Vault", body);
    }

    public boolean sendPasswordResetEmail(User user, String token) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }

        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("resetLink", "http://localhost:5173/reset-password?token=" + token);
        String body = templateEngine.process("password-reset", context);

        return sendHtml(user.getEmail(), "Reset your Evidence Vault password", body);
    }

    private boolean sendHtml(String recipient, String subject, String body) {
        if (mailHost == null || mailHost.isBlank()) {
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return false;
        }

        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromAddress);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
