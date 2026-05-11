package vn.id.luannv.lutaco.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiService {

    private static final int MAX_USER_PROMPT_LENGTH = 500;
    private static final List<String> ALLOWED_FINANCE_KEYWORDS = List.of(
            "tai chinh", "tài chính",
            "chi tieu", "chi tiêu",
            "thu nhap", "thu nhập",
            "ngan sach", "ngân sách",
            "tiet kiem", "tiết kiệm",
            "dau tu", "đầu tư",
            "lai suat", "lãi suất",
            "vay", "no", "nợ",
            "budget", "wallet", "vi tien", "ví tiền",
            "giao dich", "giao dịch", "transaction",
            "thu chi", "dòng tiền", "dong tien", "cash flow",
            "dashboard", "bao cao", "báo cáo", "report", "summary",
            "recurring", "lap lai", "lặp lại",
            "muc tieu", "mục tiêu",
            "spend", "spending", "expense", "expenses",
            "income", "revenues", "revenue",
            "saving", "savings", "investment", "investments",
            "loan", "loans", "balance"
    );

    private final ChatClient chatClient;
    private final LocalizationUtils localizationUtils;

    public GeminiService(ChatClient.Builder builder, LocalizationUtils localizationUtils) {
        this.chatClient = builder.build();
        this.localizationUtils = localizationUtils;
    }

    public String askGemini(String message) {
        String safeMessage = normalizeUserPrompt(message, "message");
        ensureFinanceRelevant(safeMessage);
        return chatClient.prompt()
                .system(buildSystemPrompt())
                .user(safeMessage)
                .call()
                .content();
    }

    public String askDashboard(String question, DashboardResponse dashboard) {
        String safeQuestion = normalizeUserPrompt(question, "question");
        ensureFinanceRelevant(safeQuestion);
        String prompt = buildDashboardPrompt(safeQuestion, dashboard);

        try {
            return askGemini(prompt);
        } catch (Exception e) {
            log.warn("[ai] dashboard analysis fallback because AI call failed: {}", e.getMessage());
            return localized("dashboard.ai.fallback") + "\n\n" + prompt;
        }
    }

    private String buildDashboardPrompt(String question, DashboardResponse dashboard) {
        String title = localized("dashboard.ai.prompt.title");
        String questionLabel = localized("dashboard.ai.prompt.question");
        String overviewLabel = localized("dashboard.ai.prompt.overview");
        String topExpenseLabel = localized("dashboard.ai.prompt.top_expense");
        String insightsLabel = localized("dashboard.ai.prompt.insights");
        String outputLabel = localized("dashboard.ai.prompt.output");
        String scopeLabel = localized("dashboard.ai.prompt.out_of_scope");
        String replyStyle = localized("dashboard.ai.prompt.reply_style");

        String insights = dashboard.getInsights() == null
                ? localized("i18n.no.message")
                : dashboard.getInsights().stream()
                .map(i -> "- [" + i.getLevel() + "] " + i.getMessage() + (i.getRecommendation() != null ? " | Gợi ý: " + i.getRecommendation() : ""))
                .collect(Collectors.joining("\n"));

        String categories = dashboard.getTopExpenseCategories() == null
                ? localized("i18n.no.message")
                : dashboard.getTopExpenseCategories().stream()
                .map(c -> "- " + c.getCategoryName() + ": " + c.getAmount() + " (" + String.format(Locale.US, "%.2f", c.getRatioNormalized()) + "%)")
                .collect(Collectors.joining("\n"));

        String incomeLabel = isVietnameseLocale() ? "Thu nhập" : "Income";
        String expenseLabel = isVietnameseLocale() ? "Chi tiêu" : "Expense";
        String balanceLabel = isVietnameseLocale() ? "Số dư" : "Balance";

        return title + "\n"
                + localized("dashboard.ai.system.role") + "\n"
                + localized("dashboard.ai.system.scope") + "\n"
                + localized("dashboard.ai.system.refuse") + "\n"
                + localized("dashboard.ai.system.response.language") + "\n"
                + localized("dashboard.ai.system.response.limit") + "\n\n"
                + outputLabel + ": " + localized("dashboard.ai.prompt.output") + "\n"
                + scopeLabel + "\n"
                + questionLabel + ": " + question + "\n\n"
                + overviewLabel + ":\n"
                + "- " + incomeLabel + ": " + dashboard.getDashboardOverview().getTotalIncome() + "\n"
                + "- " + expenseLabel + ": " + dashboard.getDashboardOverview().getTotalExpense() + "\n"
                + "- " + balanceLabel + ": " + dashboard.getDashboardOverview().getBalance() + "\n\n"
                + topExpenseLabel + ":\n" + categories + "\n\n"
                + insightsLabel + ":\n" + insights + "\n\n"
                + replyStyle;
    }

    private String normalizeUserPrompt(String value, String field) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMS, Map.of("field", field));
        }

        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new BusinessException(ErrorCode.AI_MESSAGE_BLANK);
        }
        if (normalized.length() > MAX_USER_PROMPT_LENGTH) {
            throw new BusinessException(ErrorCode.AI_MESSAGE_TOO_LONG);
        }

        return normalized;
    }

    private void ensureFinanceRelevant(String prompt) {
        String normalized = stripAccents(prompt).toLowerCase(Locale.ROOT);

        boolean matchesAllowedKeyword = ALLOWED_FINANCE_KEYWORDS.stream()
                .map(keyword -> stripAccents(keyword).toLowerCase(Locale.ROOT))
                .anyMatch(normalized::contains);

        if (!matchesAllowedKeyword) {
            throw new BusinessException(ErrorCode.AI_MESSAGE_OUT_OF_SCOPE);
        }
    }

    private String localized(String key) {
        return localizationUtils.getLocalizedMessage(key);
    }

    private boolean isVietnameseLocale() {
        return localizationUtils.getCurrentLocaleKey().toLowerCase(Locale.ROOT).startsWith("vi");
    }

    private String buildSystemPrompt() {
        return localized("dashboard.ai.system.role") + "\n"
                + localized("dashboard.ai.system.scope") + "\n"
                + localized("dashboard.ai.system.refuse") + "\n"
                + localized("dashboard.ai.system.response.language") + "\n"
                + localized("dashboard.ai.system.response.limit");
    }

    private String stripAccents(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }
}