package vn.id.luannv.lutaco.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;
import vn.id.luannv.lutaco.util.TimeUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailTemplateService {

    private static String buildEmailLayout(String title, String content, String footerInfo) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private static String buildInfoRow(String key, String value) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private static String buildFooter(String email, LocalDateTime timestamp) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    public static EmailFields sendAttentionBudget(Budget budget) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    public static EmailFields getRecurringInitializationTemplate(
            RecurringTransactionEvent.RecurringInitialization recurring) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    public static EmailFields getRecurringFrequencyTemplate(RecurringTransactionEvent.RecurringFrequency recurring) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    public static EmailFields getOtpTemplate(String email, OtpType otpType, String newCode, LocalDateTime newExpiry) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private static String formatCategoryType(CategoryType categoryType) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private static String formatFrequentType(FrequentType frequentType) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    public record EmailFields(String to, String subject, String body) {
    }
}