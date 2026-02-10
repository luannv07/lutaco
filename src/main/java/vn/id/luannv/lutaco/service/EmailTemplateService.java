package vn.id.luannv.lutaco.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailTemplateService {

    public record EmailFields(String to, String subject, String body) {}

    private static final String FONT_FAMILY = "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;";
    private static final String CARD_STYLE = "max-width:480px;margin:20px auto;background:#fff;border-radius:12px;box-shadow:0 8px 24px rgba(0,0,0,0.08);overflow:hidden;";
    private static final String FOOTER_STYLE = "background:#F8FAFC;padding:20px;text-align:center;font-size:12px;color:#94A3B8;border-top:1px solid #E2E8F0;";
    private static final String BODY_STYLE = "padding:24px;";
    private static final String INFO_ROW_STYLE = "display:flex;justify-content:space-between;padding:14px 0;border-bottom:1px solid #F1F3F9;font-size:14px;";
    private static final String INFO_KEY_STYLE = "color:#64748B;font-weight:500;";
    private static final String INFO_VALUE_STYLE = "color:#1E293B;font-weight:600;text-align:right;";

    private static String buildEmailLayout(String title, String content, String footerInfo) {
        return """
            <!DOCTYPE html><html><head><meta charset="UTF-8"><style>
            body{""" + FONT_FAMILY + """
            background-color:#F0F2F5;margin:0;padding:20px;}
            .card{""" + CARD_STYLE + """
            }.header{background:#4F46E5;padding:30px 20px;text-align:center;color:#fff;}
            .header h1{margin:0;font-size:24px;font-weight:600;}
            .body{""" + BODY_STYLE + """
            }.footer{""" + FOOTER_STYLE + """
            }.footer p{margin:4px 0;}
            .info-row{""" + INFO_ROW_STYLE + """
            }.info-row:last-child{border:none;}
            .info-key{""" + INFO_KEY_STYLE + """
            }.info-value{""" + INFO_VALUE_STYLE + """
            }.tag{background:#EEF2FF;color:#4F46E5;padding:4px 10px;border-radius:12px;font-size:12px;font-weight:700;}
            .otp-code{font-size:36px;font-weight:700;color:#4F46E5;letter-spacing:8px;font-family:'Courier New',monospace;margin:20px 0;}
            </style></head><body><div class="card"><div class="header"><h1>""" + title + """
            </h1></div><div class="body">""" + content + """
            </div><div class="footer">""" + footerInfo + """
            </div></div></body></html>""";
    }

    private static String buildInfoRow(String key, String value) {
        return "<div class=\"info-row\"><span class=\"info-key\">" + key + "</span><span class=\"info-value\">" + value + "</span></div>";
    }

    private static String buildFooter(String email, LocalDateTime timestamp) {
        return "<p>LUTACO FINANCE • © 2026</p><p>Email sent to: " + email + "</p><p>Time: " + timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>";
    }

    public static EmailFields getRecurringInitializationTemplate(RecurringTransactionEvent.RecurringInitialization recurring) {
        RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
        String subject = "LUTACO | Recurring Transaction Created";
        String title = "New Recurring Transaction";

        String content = "<p style=\"font-size:16px;color:#333;\">Hi " + fields.getFullName() + ", a new recurring transaction has been set up.</p>" +
            buildInfoRow("Amount", "<strong style=\"color:#4F46E5;\">" + String.format("%,d", fields.getAmount()) + " VND</strong>") +
            buildInfoRow("Wallet", fields.getWalletName()) +
            buildInfoRow("Frequency", "<span class=\"tag\">" + formatFrequentType(recurring.getFrequentType()) + "</span>") +
            buildInfoRow("Start Date", recurring.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) +
            buildInfoRow("Next Payment", recurring.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) +
            (fields.getNote() != null && !fields.getNote().isEmpty() ? buildInfoRow("Note", fields.getNote()) : "");

        String footer = buildFooter(fields.getEmail(), recurring.getCreatedDate());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(fields.getEmail(), subject, body);
    }

    public static EmailFields getRecurringFrequencyTemplate(RecurringTransactionEvent.RecurringFrequency recurring) {
        RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
        String subject = "LUTACO | Recurring Transaction Executed";
        String title = "Transaction Automatically Created";

        String content = "<p style=\"font-size:16px;color:#333;\">Hi " + fields.getFullName() + ", a scheduled transaction has been automatically created for you.</p>" +
            buildInfoRow("Amount", "<strong style=\"color:#28a745;\">" + String.format("%,d", fields.getAmount()) + " VND</strong>") +
            buildInfoRow("Wallet", fields.getWalletName()) +
            buildInfoRow("Original Transaction ID", "<code style=\"font-size:12px;\">" + fields.getTransactionId() + "</code>") +
            buildInfoRow("Next Scheduled Payment", recurring.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) +
            (fields.getNote() != null && !fields.getNote().isEmpty() ? buildInfoRow("Note", fields.getNote()) : "");

        String footer = buildFooter(fields.getEmail(), LocalDateTime.now());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(fields.getEmail(), subject, body);
    }

    public static EmailFields getOtpTemplate(String email, OtpType otpType, String newCode, LocalDateTime newExpiry) {
        String subject = "LUTACO | Your OTP Code for " + otpType.name();
        String title = "Account Verification";

        String content = "<p style=\"text-align:center;font-size:16px;color:#333;\">Use the code below to complete your verification.</p>" +
            "<div style=\"text-align:center;\"><div class=\"otp-code\">" + newCode + "</div></div>" +
            "<p style=\"text-align:center;color:#64748B;\">This code will expire at " + newExpiry.format(DateTimeFormatter.ofPattern("HH:mm:ss, dd/MM/yyyy")) + ".</p>";

        String footer = buildFooter(email, LocalDateTime.now());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(email, subject, body);
    }

    private static String formatFrequentType(FrequentType frequentType) {
        if (frequentType == null) return "N/A";
        return switch (frequentType) {
            case DAILY -> "Daily";
            case WEEKLY -> "Weekly";
            case MONTHLY -> "Monthly";
            case YEARLY -> "Yearly";
        };
    }
}
