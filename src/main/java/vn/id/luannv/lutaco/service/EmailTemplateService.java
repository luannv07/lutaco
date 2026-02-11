package vn.id.luannv.lutaco.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;
import vn.id.luannv.lutaco.util.TimeUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailTemplateService {

    public record EmailFields(String to, String subject, String body) {}

    private static String buildEmailLayout(String title, String content, String footerInfo) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>"
                // General
                + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f3f4f6; margin: 0; padding: 16px; }"
                + ".card { max-width: 560px; margin: 20px auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.06), 0 10px 10px -5px rgba(0,0,0,0.04); border: 1px solid #e5e7eb; }"
                // Header
                + ".header { padding: 24px 32px; background-color: #2563eb; color: #ffffff; border-top-left-radius: 12px; border-top-right-radius: 12px; text-align: center; }"
                + ".header h1 { margin: 0; font-size: 24px; font-weight: 600; }"
                // Body
                + ".body { padding: 18px 12px; }" // Adjusted padding
                // Greeting & Paragraph
                + ".greeting { font-size: 18px; font-weight: 600; color: #1f2937; margin-bottom: 8px; }"
                + ".paragraph { font-size: 15px; color: #4b5563; line-height: 1.7; margin-bottom: 24px; }"
                // Info Table
                + ".info-table { width: 100%; border-collapse: collapse; }"
                + ".info-table td { padding: 16px 0; border-bottom: 1px solid #e5e7eb; font-size: 14px; }"
                + ".info-table tr:last-child td { border-bottom: none; }"
                + ".info-key { color: #6b7280; padding-right: 16px; }"
                + ".info-value { color: #1f2937; font-weight: 600; text-align: right; }"
                // Tag & OTP
                + ".tag { display: inline-block; background-color: #dbeafe; color: #2563eb; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; }"
                + ".otp-code { font-family: 'Courier New', monospace; font-size: 44px; font-weight: 700; color: #1e3a8a; letter-spacing: 10px; margin: 24px 0; text-align: center; background-color: #f3f4f6; padding: 24px; border-radius: 12px; border: 1px solid #d1d5db; }"
                // Footer
                + ".footer { padding: 12px; text-align: center; font-size: 12px; color: #9ca3af; border-top: 1px solid #e5e7eb; }"
                + ".footer p { margin: 5px 0; }"
                + ".footer .brand { color: #2563eb; font-weight: 600; }"
                + "</style></head><body><div class=\"card\">"
                + "<div class=\"header\"><h1>" + title + "</h1></div>"
                + "<div class=\"body\">" + content + "</div>"
                + "<div class=\"footer\">" + footerInfo + "</div>"
                + "</div></body></html>";
    }

    private static String buildInfoRow(String key, String value) {
        return "<tr><td class=\"info-key\">" + key + "</td><td class=\"info-value\">" + value + "</td></tr>";
    }

    private static String buildFooter(String email, LocalDateTime timestamp) {
        return "<p><span class=\"brand\">LUTACO</span> • © 2099</p><p>Email này được gửi tự động đến " + email + "</p><p>Thời gian: " + TimeUtils.formatToUserZone(timestamp, TimeUtils.DEFAULT_TIMEZONE, "HH:mm, dd/MM/yyyy") + "</p>";
    }

    public static EmailFields getRecurringInitializationTemplate(RecurringTransactionEvent.RecurringInitialization recurring) {
        RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
        String title = "Giao Dịch Định Kỳ #" + recurring.getRecurringTransactionId() + " Đã Được Lên Lịch";
        String subject = "LUTACO | " + title;

        String content = "<p class=\"greeting\">Xin chào con vợ <span style=\"color:#1d4ed8 !important;\">" + fields.getFullName() + "</span>,</p>"
                + "<p class=\"paragraph\">Một giao dịch định kỳ đã được thiết lập và sẽ được tự động thực hiện theo lịch trình bên dưới.</p>"
                + "<table class=\"info-table\">"
                + buildInfoRow("Mã giao dịch kỳ", "<strong>#" + recurring.getRecurringTransactionId() + "</strong>")
                + buildInfoRow("Số tiền", "<strong style=\"color:#1d4ed8; font-size: 16px;\">" + String.format("%,d", fields.getAmount()) + " VND</strong>")
                + buildInfoRow("Thu/Chi", formatCategoryType(fields.getCategoryType()))
                + buildInfoRow("Danh mục", fields.getCategoryName())
                + buildInfoRow("Từ ví", fields.getWalletName())
                + buildInfoRow("Tần suất", "<span class=\"tag\">" + formatFrequentType(recurring.getFrequentType()) + "</span>")
                + buildInfoRow("Ngày bắt đầu", recurring.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                + buildInfoRow("Kỳ tiếp theo", "<strong>" + recurring.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</strong>")
                + (fields.getNote() != null && !fields.getNote().isEmpty() ? buildInfoRow("Ghi chú", fields.getNote()) : "")
                + "</table>";

        String footer = buildFooter(fields.getEmail(), recurring.getCreatedDate());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(fields.getEmail(), subject, body);
    }

    public static EmailFields getRecurringFrequencyTemplate(RecurringTransactionEvent.RecurringFrequency recurring) {
        RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
        String title = "Giao Dịch #" + recurring.getRecurringTransactionId() + " Vừa Được Ghi Nhận";
        String subject = "LUTACO | " + title;

        String content = "<p class=\"greeting\">Xin chào con vợ <span style=\"color:#1d4ed8 !important;\">" + fields.getFullName() + "</span>,</p>"
                + "<p class=\"paragraph\">Hệ thống vừa tự động ghi nhận một giao dịch mới từ lịch định kỳ của bạn. Dưới đây là chi tiết:</p>"
                + "<table class=\"info-table\">"
                + buildInfoRow("Mã định kỳ", "<strong>#" + recurring.getRecurringTransactionId() + "</strong>")
                + buildInfoRow("Số tiền", "<strong style=\"font-size: 16px; color: #1f2937;\">" + String.format("%,d", fields.getAmount()) + " VND</strong>")
                + buildInfoRow("Thu/Chi", formatCategoryType(fields.getCategoryType()))
                + buildInfoRow("Danh mục", fields.getCategoryName())
                + buildInfoRow("Ví", fields.getWalletName())
                + buildInfoRow("Kỳ thanh toán tiếp theo", "<strong>" + recurring.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</strong>")
                + (fields.getNote() != null && !fields.getNote().isEmpty() ? buildInfoRow("Ghi chú", fields.getNote()) : "")
                + "</table>";

        String footer = buildFooter(fields.getEmail(), LocalDateTime.now());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(fields.getEmail(), subject, body);
    }

    public static EmailFields getOtpTemplate(String email, OtpType otpType, String newCode, LocalDateTime newExpiry) {
        String subject = "LUTACO | Mã Xác Thực OTP";
        String title = "Mã Xác Thực OTP";

        String content = "<p class=\"greeting\" style=\"text-align:center;\">Vui lòng sử dụng mã dưới đây để hoàn tất xác thực.</p>"
                + "<div class=\"otp-code\">" + newCode + "</div>"
                + "<p class=\"paragraph\" style=\"text-align:center; margin-top: 20px;\">Mã sẽ hết hạn lúc " + TimeUtils.formatToUserZone(newExpiry, TimeUtils.DEFAULT_TIMEZONE, "HH:mm, dd/MM/yyyy") + ".</p>"
                + "<p class=\"paragraph\" style=\"text-align:center; font-size: 13px; color: #9ca3af;\">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.</p>";

        String footer = buildFooter(email, LocalDateTime.now());
        String body = buildEmailLayout(title, content, footer);

        return new EmailFields(email, subject, body);
    }

    private static String formatCategoryType(CategoryType categoryType) {
        if (categoryType == null) return "Không xác định";
        return switch (categoryType) {
            case INCOME -> "Thu nhập";
            case EXPENSE -> "Chi tiêu";
        };
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
