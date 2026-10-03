package com.campus.events.service;

import com.campus.events.model.EventRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Async
    public void sendRegistrationConfirmation(EventRegistration registration) {
        String subject = "Event Registration Confirmation: " + registration.getEvent().getTitle();
        String body = String.format(
                "Hello %s,\n\n" +
                "Thank you for registering for \"%s\" hosted by %s!\n\n" +
                "Event Details:\n" +
                "- Date & Time: %s\n" +
                "- Location: %s\n" +
                "- Ticket Code: %s\n\n" +
                "Please present your ticket code or digital entry pass upon arrival.\n\n" +
                "Best regards,\n" +
                "Campus Event Management",
                registration.getStudentName(),
                registration.getEvent().getTitle(),
                registration.getEvent().getClubName(),
                registration.getEvent().getEventDate(),
                registration.getEvent().getLocation(),
                registration.getTicketCode()
        );

        boolean isSmtpConfigured = mailHost != null && !mailHost.trim().isEmpty() && !mailHost.contains("your-smtp");

        if (mailSender != null && isSmtpConfigured) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                if (mailUsername != null && !mailUsername.trim().isEmpty()) {
                    message.setFrom(mailUsername);
                } else {
                    message.setFrom("noreply@campus.edu");
                }
                message.setTo(registration.getStudentEmail());
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                logger.info("Confirmation email sent to {}", registration.getStudentEmail());
                return;
            } catch (Exception e) {
                logger.warn("Failed to send email via SMTP ({}). Falling back to console log.", e.getMessage());
            }
        }

        // Fallback: log to console
        logger.info("=== CONFIRMATION EMAIL (CONSOLE FALLBACK) ===");
        logger.info("To: {}", registration.getStudentEmail());
        logger.info("Subject: {}", subject);
        logger.info("Body:\n{}", body);
        logger.info("==========================================");
    }
}
