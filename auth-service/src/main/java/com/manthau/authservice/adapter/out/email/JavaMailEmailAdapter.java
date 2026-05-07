package com.manthau.authservice.adapter.out.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JavaMailEmailAdapter {

    private final JavaMailSender mailSender;

    @Async
    public void send(String toEmail, String verifyUrl) {
        try {
            String html = """
                    <p>Welcome! Please verify your email address:</p>
                    <p><a href="%s">Verify Email</a></p>
                    <p>This link expires in 24 hours.</p>
                    """.formatted(verifyUrl);

            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(toEmail);
            helper.setSubject("Verify your email");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}
