package vn.id.luannv.lutaco.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;
import vn.id.luannv.lutaco.service.AsyncEmailService;
import vn.id.luannv.lutaco.service.TransactionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecurringTransactionListener {
    AsyncEmailService emailService;
    TransactionService transactionService;

    /**
     * Xử lý sự kiện tạo giao dịch định kỳ mới
     * Gửi email xác nhận cho người dùng
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void sendRecurringTransactionInitializationEmail(
            RecurringTransactionEvent.RecurringInitialization recurring) {
        try {
            log.info("Processing RecurringInitialization event for transaction: {}",
                    recurring.getRecurringUserFields().getTransactionId());

            RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
            LocalDate startDate = recurring.getStartDate();
            LocalDate nextPaymentDate = recurring.getNextPaymentDate();
            LocalDateTime createdDate = recurring.getCreatedDate();
            FrequentType frequentType = recurring.getFrequentType();

            String fullName = fields.getFullName();
            String walletName = fields.getWalletName();
            String note = fields.getNote();
            String transactionId = fields.getTransactionId();
            String email = fields.getEmail();
            Long amount = fields.getAmount();
            String currency = "VND";

            // Template email cho RecurringInitialization
            String body = """
                    <!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','Helvetica Neue',Arial,sans-serif;background-color:#f5f5f5;margin:0;padding:10px}.container{max-width:600px;background:#fff;margin:0 auto;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08)}.header{background:linear-gradient(135deg,#667eea 0,#764ba2 100%);padding:20px 12px;text-align:center}.header h1{margin:0;color:#fff;font-size:24px;font-weight:600}.content{padding:20px 24px}.greeting{font-size:16px;color:#333;margin-bottom:20px;line-height:1.5;font-weight:500}.info-box{background:#f8f9fa;border-left:4px solid #667eea;border-radius:6px;padding:16px;margin:20px 0}.info-row{display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #e9ecef;font-size:14px}.info-row:last-child{border-bottom:none}.info-label{color:#6c757d;font-weight:500}.info-value{color:#333;font-weight:600;text-align:right}.alert{background:#e7f3ff;border-left:3px solid #2196F3;padding:12px 16px;margin:16px 0;border-radius:4px;font-size:13px;color:#1565c0;line-height:1.5}.footer{background:#f8f9fa;padding:16px 24px;text-align:center;border-top:1px solid #e9ecef}.footer p{margin:6px 0;font-size:12px;color:#6c757d}.brand{font-weight:600;color:#667eea;font-size:14px}.divider{height:1px;background:#e9ecef;margin:16px 0}.amount-value{color:#28a745;font-weight:700;font-size:16px}.frequency-badge{display:inline-block;background:#e3f2fd;color:#1976d2;padding:4px 10px;border-radius:15px;font-size:12px;font-weight:600}</style></head><body><div class="container"><div class="header"><h1>📅 Giao dịch Định kỳ Được Tạo</h1></div><div class="content"><p class="greeting">Xin chào <strong>""" + fullName + """
                    </strong>,</p><p class="greeting">Giao dịch định kỳ của bạn đã được thiết lập thành công. Dưới đây là chi tiết chi tiết:</p><div class="info-box"><div class="info-row"><span class="info-label">👤 Tên</span><span class="info-value">""" + fullName + """
                    </span></div><div class="info-row"><span class="info-label">💰 Ví</span><span class="info-value">""" + walletName + """
                    </span></div><div class="info-row"><span class="info-label">💵 Số tiền</span><span class="info-value amount-value">""" + String.format("%,d", amount) + " " + currency + """
                    </span></div><div class="info-row"><span class="info-label">📝 Ghi chú</span><span class="info-value">""" + (note != null && !note.isEmpty() ? note : "<em style=\"color:#999\">Không có</em>") + """
                    </span></div><div class="info-row"><span class="info-label">🔄 Loại định kỳ</span><span class="info-value"><span class="frequency-badge">""" + formatFrequentType(frequentType) + """
                    </span></span></div><div class="info-row"><span class="info-label">📅 Ngày bắt đầu</span><span class="info-value">""" + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + """
                    </span></div><div class="info-row"><span class="info-label">⏰ Thanh toán tiếp theo</span><span class="info-value">""" + nextPaymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + """
                    </span></div><div class="info-row"><span class="info-label">🆔 Mã giao dịch</span><span class="info-value" style="font-family:'Courier New',monospace;font-size:12px">""" + transactionId + """
                    </span></div><div class="info-row"><span class="info-label">⏱️ Tạo lúc</span><span class="info-value">""" + createdDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                    </span></div></div><div class="alert">⚠️ <strong>Lưu ý quan trọng:</strong> Giao dịch này sẽ được thực hiện tự động vào ngày thanh toán tiếp theo theo định kỳ """
                    + formatFrequentType(frequentType).toLowerCase() + """
                    . Bạn có thể hủy hoặc chỉnh sửa nó bất kỳ lúc nào tại ứng dụng LUTACO.</div><div class="divider"></div><p style="font-size:14px;color:#333;line-height:1.6">Nếu bạn không tạo giao dịch này hoặc có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi ngay lập tức.</p><div class="footer"><p class="brand">LUTACO - Quản lý tài chính cá nhân</p><p>© 2026 Lutaco | Luận & Tuân</p><p style="color:#adb5bd;font-size:11px">Email được gửi vào: """ + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                    </p><p style="color:#adb5bd;font-size:11px">Địa chỉ email: """ + email + """
                    </p></div></div></body></html>
                    """;

            // Gửi email
            emailService.sendEmail(
                    email,
                    "LUTACO | Giao dịch định kỳ được tạo thành công",
                    body
            );

            log.info("Successfully sent RecurringInitialization email to: {}", email);

        } catch (Exception e) {
            log.error("Error sending RecurringInitialization email", e);
        }
    }

    /**
     * Xử lý sự kiện giao dịch định kỳ được thực hiện
     * Gửi email thông báo cho người dùng về giao dịch đã xảy ra
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void sendRecurringTransactionFrequencyEmail(
            RecurringTransactionEvent.RecurringFrequency recurring) {
        transactionService
                .autoCreateTransactionWithCronJob(recurring.getRecurringUserFields().getTransactionId(), recurring.getRecurringUserFields().getUserId());
        try {
            log.info("Processing RecurringFrequency event for transaction: {}",
                    recurring.getRecurringUserFields().getTransactionId());

            RecurringTransactionEvent.RecurringUserFields fields = recurring.getRecurringUserFields();
            LocalDate nextPaymentDate = recurring.getNextPaymentDate();
            FrequentType frequentType = recurring.getFrequentType();

            String fullName = fields.getFullName();
            String walletName = fields.getWalletName();
            String note = fields.getNote();
            String transactionId = fields.getTransactionId();
            String email = fields.getEmail();
            Long amount = fields.getAmount();
            String currency = "VND";

            // Template email cho RecurringFrequency
            String body = """
                    <!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','Helvetica Neue',Arial,sans-serif;background-color:#f5f5f5;margin:0;padding:10px}.container{max-width:600px;background:#fff;margin:0 auto;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08)}.header{background:linear-gradient(135deg,#28a745 0,#20c997 100%);padding:20px 12px;text-align:center}.header h1{margin:0;color:#fff;font-size:24px;font-weight:600}.content{padding:20px 24px}.greeting{font-size:16px;color:#333;margin-bottom:20px;line-height:1.5;font-weight:500}.info-box{background:#f8f9fa;border-left:4px solid #28a745;border-radius:6px;padding:16px;margin:20px 0}.info-row{display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #e9ecef;font-size:14px}.info-row:last-child{border-bottom:none}.info-label{color:#6c757d;font-weight:500}.info-value{color:#333;font-weight:600;text-align:right}.success-badge{display:inline-block;background:#d4edda;color:#155724;padding:8px 12px;border-radius:6px;font-size:13px;font-weight:600;margin:10px 0}.alert{background:#f0f7ff;border-left:3px solid #28a745;padding:12px 16px;margin:16px 0;border-radius:4px;font-size:13px;color:#155724;line-height:1.5}.footer{background:#f8f9fa;padding:16px 24px;text-align:center;border-top:1px solid #e9ecef}.footer p{margin:6px 0;font-size:12px;color:#6c757d}.brand{font-weight:600;color:#667eea;font-size:14px}.divider{height:1px;background:#e9ecef;margin:16px 0}.amount-value{color:#28a745;font-weight:700;font-size:16px}.frequency-badge{display:inline-block;background:#e3f2fd;color:#1976d2;padding:4px 10px;border-radius:15px;font-size:12px;font-weight:600}</style></head><body><div class="container"><div class="header"><h1>✅ Giao dịch Định kỳ Được Thực hiện</h1></div><div class="content"><p class="greeting">Xin chào <strong>""" + fullName + """
                    </strong>,</p><p class="greeting">Giao dịch định kỳ của bạn đã được thực hiện thành công. Chi tiết như sau:</p><div class="success-badge">✓ Giao dịch được thực hiện vào: """ + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                    </div><div class="info-box"><div class="info-row"><span class="info-label">👤 Tên</span><span class="info-value">""" + fullName + """
                    </span></div><div class="info-row"><span class="info-label">💰 Ví</span><span class="info-value">""" + walletName + """
                    </span></div><div class="info-row"><span class="info-label">💵 Số tiền</span><span class="info-value amount-value">""" + String.format("%,d", amount) + " " + currency + """
                    </span></div><div class="info-row"><span class="info-label">📝 Ghi chú</span><span class="info-value">""" + (note != null && !note.isEmpty() ? note : "<em style=\"color:#999\">Không có</em>") + """
                    </span></div><div class="info-row"><span class="info-label">🔄 Loại định kỳ</span><span class="info-value"><span class="frequency-badge">""" + formatFrequentType(frequentType) + """
                    </span></span></div><div class="info-row"><span class="info-label">⏰ Lần thanh toán tiếp theo</span><span class="info-value">""" + nextPaymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + """
                    </span></div><div class="info-row"><span class="info-label">🆔 Mã giao dịch</span><span class="info-value" style="font-family:'Courier New',monospace;font-size:12px">""" + transactionId + """
                    </span></div></div><div class="alert">ℹ️ <strong>Thông tin:</strong> Giao dịch định kỳ tiếp theo sẽ được thực hiện vào ngày <strong>""" + nextPaymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + """
                    </strong>. Bạn có thể quản lý hoặc hủy giao dịch này tại ứng dụng LUTACO bất kỳ lúc nào.</div><div class="divider"></div><p style="font-size:14px;color:#333;line-height:1.6">Cảm ơn bạn đã sử dụng LUTACO. Nếu bạn có bất kỳ câu hỏi hoặc nhu cầu hỗ trợ, vui lòng liên hệ với chúng tôi.</p><div class="footer"><p class="brand">LUTACO - Quản lý tài chính cá nhân</p><p>© 2026 Lutaco | Luận & Tuân</p><p style="color:#adb5bd;font-size:11px">Email được gửi vào: """ + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                    </p><p style="color:#adb5bd;font-size:11px">Địa chỉ email: """ + email + """
                    </p></div></div></body></html>
                    """;

            // Gửi email
            emailService.sendEmail(
                    email,
                    "LUTACO | Giao dịch định kỳ được thực hiện",
                    body
            );

            log.info("Successfully sent RecurringFrequency email to: {}", email);

        } catch (Exception e) {
            log.error("Error sending RecurringFrequency email", e);
        }
    }

    /**
     * Format FrequentType thành text tiếng Việt
     */
    private String formatFrequentType(FrequentType frequentType) {
        if (frequentType == null) {
            return "Không xác định";
        }
        return switch (frequentType) {
            case DAILY -> "Hàng ngày";
            case WEEKLY -> "Hàng tuần";
            case MONTHLY -> "Hàng tháng";
            case YEARLY -> "Hàng năm";
            default -> frequentType.toString();
        };
    }
}