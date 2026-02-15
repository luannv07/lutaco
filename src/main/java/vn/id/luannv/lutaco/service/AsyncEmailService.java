package vn.id.luannv.lutaco.service;

import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AsyncEmailService {
    JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String body) {
        log.info("Attempting to send email to '{}' with subject '{}'.", to, subject);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            long startTime = System.currentTimeMillis();
            mailSender.send(mimeMessage);
            long endTime = System.currentTimeMillis();
            log.info("Successfully sent email to '{}'. Time taken: {}ms.", to, (endTime - startTime));

        } catch (Exception e) {
            log.error("Failed to send email to '{}' with subject '{}'. Error: {}", to, subject, e.getMessage(), e);
        }
    }
}
