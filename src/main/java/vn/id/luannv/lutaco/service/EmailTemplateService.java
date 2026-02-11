package vn.id.luannv.lutaco.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailTemplateService {

    public record EmailFields(String to, String subject, String body) {}

    private static String buildEmailLayout(String title, String content, String footerInfo) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>"
                + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8f9fa; margin: 0; padding: 20px; }"
                + ".card { max-width: 520px; margin: 20px auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 12px 32px rgba(0,0,0,0.08); overflow: hidden; }"
                + ".header { padding: 32px; text-align: center; background: linear-gradient(135deg, #5f38c8 0%, #3f72af 100%); color: #ffffff; }"
                + ".header h1 { margin: 0; font-size: 26px; font-weight: 600; }"
                + ".body { padding: 30px; }"
                + ".greeting { font-size: 18px; font-weight: 600; color: #343a40; margin-bottom: 10px; }"
                + ".paragraph { font-size: 15px; color: #495057; line-height: 1.65; margin-bottom: 24px; }"
                + ".info-table { width: 100%; border-collapse: collapse; }"
                + ".info-table td { padding: 15px 0; border-bottom: 1px solid #e9ecef; font-size: 14px; }"
                + ".info-table tr:last-child td { border-bottom: none; }"
                + ".info-key { color: #6c757d; padding-right: 16px; }"
                + ".info-value { color: #212529; font-weight: 600; text-align: right; }"
                + ".tag { display: inline-block; background-color: #f1f3f5; color: #495057; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; }"
                + ".otp-code { font-size: 42px; font-weight: 700; color: #5f38c8; letter-spacing: 10px; margin: 24px 0; text-align: center; background-color: #f8f9fa; padding: 20px; border-radius: 12px; }"
                + ".footer { padding: 24px; text-align: center; font-size: 12px; color: #adb5bd; border-top: 1px solid #e9ecef; }"
                + ".footer p { margin: 5px 0; }"
                + "</style></head><body><div class=\"card\"><div class=\"header\"><h1>" + title + "</h1></div>"
                + "<div class=\"body\">" + content + "</div>"
                + "<div class=\"footer\">" + footerInfo + "</div></div></body></html>";
    }

    private static String buildInfoRow(String key, String value) {
        return "<tr><td class=\"info-key\">" + key + "</td><td class=\"info-value\">" + value + "</td></tr>";
    }

    private static String buildFooter(String email, LocalDateTime timestamp) {
        return "<p>LUTACO FINANCE • © 2024</p><p>Email này được gửi tự động đến " + email + "</p><p>Thời gian: " + timestamp.format(DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy")) + "</p>";
    }

    public static EmailFields getRecurringInitializationTemplate(RecurringTransactionEvent.RecurringInitialization recurring) {
        RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
        String subject = "LUTACO | Giao Dịch Định Kỳ Của Bạn Đã Được Lên Lịch";
        String title = "Đã Lên Lịch Thành Công";

        String content = "<p class=\"greeting\">Xin chào " + fields.getFullName() + ",</p>"
                + "<p class=\"paragraph\">Một giao dịch định kỳ mới đã được thiết lập. Chúng tôi sẽ tự động thực hiện giao dịch này cho bạn theo lịch trình bên dưới.</p>"
                + "<table class=\"info-table\">"
                + buildInfoRow("Số tiền", "<strong style=\"color:#5f38c8; font-size: 15px;\">" + String.format("%,d", fields.getAmount()) + " VND</strong>")
                + buildInfoRow("Ví", fields.getWalletName())
                + buildInfoRow("Tần suất", "<span class=\"tag\">" + formatFrequentType(recurring.getFrequentType()) + "</span>")
                + buildInfoRow("Ngày bắt đầu", recurring.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                + buildInfoRow("Thanh toán tiếp theo", "<strong>" + recurring.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</strong>")
                + (fields.getNote() != null && !fields.getNote().isEmpty() ? buildInfoRow("Ghi chú", fields.getNote()) : "")
                + "</table>";

        String footer = buildFooter(fields.getEmail(), recurring.getCreatedDate());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(fields.getEmail(), subject, body);
    }

    public static EmailFields getRecurringFrequencyTemplate(RecurringTransactionEvent.RecurringFrequency recurring) {
        RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
        String subject = "LUTACO | Giao Dịch Tự Động Vừa Được Thực Hiện";
        String title = "Giao Dịch Vừa Thực Hiện";

        String content = "<p class=\"greeting\">Xin chào " + fields.getFullName() + ",</p>"
                + "<p class=\"paragraph\">Giao dịch định kỳ của bạn vừa được hệ thống tự động thực hiện. Dưới đây là chi tiết giao dịch:</p>"
                + "<table class=\"info-table\">"
                + buildInfoRow("Số tiền", "<strong style=\"color:#2B8A3E; font-size: 15px;\">" + String.format("%,d", fields.getAmount()) + " VND</strong>")
                + buildInfoRow("Ví", fields.getWalletName())
                + buildInfoRow("Lịch thanh toán tiếp theo", "<strong>" + recurring.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</strong>")
                + (fields.getNote() != null && !fields.getNote().isEmpty() ? buildInfoRow("Ghi chú", fields.getNote()) : "")
                + "</table>";

        String footer = buildFooter(fields.getEmail(), LocalDateTime.now());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(fields.getEmail(), subject, body);
    }

    public static EmailFields getOtpTemplate(String email, OtpType otpType, String newCode, LocalDateTime newExpiry) {
        String subject = "LUTACO | Mã Xác Thực (OTP) Của Bạn";
        String title = "Mã Xác Thực Của Bạn";

        String content = "<p class=\"greeting\" style=\"text-align:center;\">Mã xác thực của bạn là:</p>"
                + "<div class=\"otp-code\">" + newCode + "</div>"
                + "<p class=\"paragraph\" style=\"text-align:center; margin-top: 20px;\">Vui lòng sử dụng mã này để hoàn tất. Mã sẽ hết hạn lúc " + newExpiry.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")) + ".</p>";

        String footer = buildFooter(email, LocalDateTime.now());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(email, subject, body);
    }

    private static String formatFrequentType(FrequentType frequentType) {
        if (frequentType == null) return "Không xác định";
        return switch (frequentType) {
            case DAILY -> "Hàng ngày";
            case WEEKLY -> "Hàng tuần";
            case MONTHLY -> "Hàng tháng";
            case YEARLY -> "Hàng năm";
        };
    }
}
