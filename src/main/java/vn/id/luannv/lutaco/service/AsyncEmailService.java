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

            Long beforeSend = System.currentTimeMillis();
            log.info("✉️ Sending email to {}", to);
            mailSender.send(mimeMessage);
            Long afterSend = System.currentTimeMillis();
            log.info("📩 Sent email to {}, time execute: {}ms", to, afterSend - beforeSend);

        } catch (Exception e) {
            log.error("Send email failed to {}", to, e);
        }
    }
}
