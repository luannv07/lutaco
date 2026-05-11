package vn.id.luannv.lutaco.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import vn.id.luannv.lutaco.dto.response.AiExtractResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiService {

    private static final int MAX_USER_PROMPT_LENGTH = 500;
    private static final int MAX_DASHBOARD_PROMPT_LENGTH = 4000;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
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
    private final ObjectMapper objectMapper;

    public GeminiService(ChatClient.Builder builder, LocalizationUtils localizationUtils, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.localizationUtils = localizationUtils;
        this.objectMapper = objectMapper;
    }

    public String askGemini(String message) {
        String safeMessage = normalizeUserPrompt(message, "message");
        ensureFinanceRelevant(safeMessage);
        return callModel(buildSystemPrompt(), safeMessage);
    }

    public String askDashboard(String question, DashboardResponse dashboard) {
        return askDashboard(question, dashboard, null);
    }

    public String askDashboard(String question, DashboardResponse dashboard, String currentUsername) {
        String safeQuestion = normalizeUserPrompt(question, "question");
        ensureFinanceRelevant(safeQuestion);
        String safeUserName = sanitizeDisplayName(currentUsername);
        String prompt = clampDashboardPrompt(buildDashboardPrompt(safeQuestion, dashboard, safeUserName));

        try {
            String rawAnswer = callModel(buildSystemPrompt(), prompt);
            return normalizeDashboardAnswer(rawAnswer, safeUserName);
        } catch (Exception e) {
            log.warn("[ai] dashboard analysis fallback because AI call failed: {}", e.getMessage());
            return buildLocalDashboardFallback(dashboard, safeUserName);
        }
    }

    public AiExtractResponse extractTransactionFromImage(MultipartFile file) {
        validateExtractionFile(file);

        String rawAnswer;
        try {
            rawAnswer = chatClient.prompt()
                    .system(buildExtractionSystemPrompt())
                    .user(user -> user
                            .text(buildExtractionUserPrompt())
                            .media(resolveMimeType(file), toImageResource(file)))
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("[ai] image extraction failed because AI call failed: {}", e.getMessage());
            return buildFailedExtractionResponse(null, "AI extraction failed. Please try again later.");
        }

        return parseExtractionResponse(rawAnswer);
    }

    private String buildDashboardPrompt(String question, DashboardResponse dashboard, String currentUsername) {
        String title = localized("dashboard.ai.prompt.title");
        String questionLabel = localized("dashboard.ai.prompt.question");
        String overviewLabel = localized("dashboard.ai.prompt.overview");
        String topExpenseLabel = localized("dashboard.ai.prompt.top_expense");
        String insightsLabel = localized("dashboard.ai.prompt.insights");
        String scopeLabel = localized("dashboard.ai.prompt.out_of_scope");
        String replyStyle = localized("dashboard.ai.prompt.reply_style");
        String greetingRule = localized("dashboard.ai.prompt.greeting.rule", currentUsername);
        String outputFormatRule = localized("dashboard.ai.prompt.output.format", currentUsername);

        String insights = dashboard.getInsights() == null || dashboard.getInsights().isEmpty()
                ? localized("dashboard.ai.prompt.no_insight")
                : dashboard.getInsights().stream()
                .map(i -> "- [" + i.getLevel() + "] " + i.getMessage() + (i.getRecommendation() != null ? " | Gợi ý: " + i.getRecommendation() : ""))
                .collect(Collectors.joining("\n"));

        String categories = dashboard.getTopExpenseCategories() == null || dashboard.getTopExpenseCategories().isEmpty()
                ? localized("dashboard.ai.prompt.no_data")
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
                + localized("dashboard.ai.prompt.output") + "\n"
                + greetingRule + "\n"
                + outputFormatRule + "\n"
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

    private String clampDashboardPrompt(String prompt) {
        if (prompt == null) {
            return "";
        }
        if (prompt.length() <= MAX_DASHBOARD_PROMPT_LENGTH) {
            return prompt;
        }
        return prompt.substring(0, MAX_DASHBOARD_PROMPT_LENGTH)
                + "\n\n"
                + localized("dashboard.ai.prompt.truncated");
    }

    private String buildExtractionSystemPrompt() {
        return "You are a finance document extraction engine. "
                + "Extract the content from the provided image and return ONLY valid JSON. "
                + "Do not add markdown, code fences, explanations, or extra keys. "
                + "If a value is missing, use null. "
                + "Amounts must be numbers in VND. "
                + "If the document is not a bill or a transfer receipt, set success to false and provide a short error message.";
    }

    private String buildExtractionUserPrompt() {
        return "Return JSON with this shape: {"
                + "\"transactionDraft\": {"
                + "\"amount\": 1430000, "
                + "\"transactionDate\": \"2026-05-11T10:15:49.187176+00:00\", "
                + "\"note\": \"MB Bank - Chuyển tiền thành công từ DANG THANH NGA\", "
                + "\"suggestedCategory\": \"other\""
                + "}, "
                + "\"bill\": {"
                + "\"storeName\": \"MB Bank\", "
                + "\"storeAddress\": null, "
                + "\"date\": \"24/04/2025\", "
                + "\"time\": \"11:57\", "
                + "\"items\": [], "
                + "\"subtotal\": null, "
                + "\"discount\": null, "
                + "\"tax\": null, "
                + "\"total\": 1430000, "
                + "\"currency\": \"VND\", "
                + "\"paymentMethod\": \"Bank Transfer\", "
                + "\"category\": \"other\", "
                + "\"notes\": \"Chuyển tiền thành công từ DANG THANH NGA\""
                + "}, "
                + "\"error\": null }";
    }

    private AiExtractResponse parseExtractionResponse(String rawAnswer) {
        String rawText = rawAnswer == null ? "" : rawAnswer.trim();
        if (rawText.isBlank()) {
            return buildFailedExtractionResponse(rawText, "AI returned an empty response.");
        }

        String jsonPayload = extractJsonPayload(rawText);
        try {
            AiExtractResponse response = objectMapper.readValue(jsonPayload, AiExtractResponse.class);
            response.setRawText(rawText);
            return response;
        } catch (Exception e) {
            log.warn("[ai] unable to parse extraction response as JSON: {}", e.getMessage());
            return buildFailedExtractionResponse(rawText, "AI returned invalid JSON.");
        }
    }

    private AiExtractResponse buildFailedExtractionResponse(String rawText, String error) {
        return AiExtractResponse.builder()
                .rawText(rawText)
                .error(error)
                .build();
    }

    private String extractJsonPayload(String rawText) {
        String cleaned = rawText
                .replaceFirst("^```(?:json)?\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }

        return cleaned;
    }

    private void validateExtractionFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.AI_IMAGE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(ErrorCode.AI_IMAGE_INVALID);
        }
    }

    private MimeType resolveMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return MimeType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }

        return MimeType.valueOf(contentType);
    }

    private Resource toImageResource(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            return new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    String originalFilename = file.getOriginalFilename();
                    return originalFilename != null && !originalFilename.isBlank() ? originalFilename : "upload-image";
                }
            };
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private String buildLocalDashboardFallback(DashboardResponse dashboard, String currentUsername) {
        StringBuilder builder = new StringBuilder(localized("dashboard.ai.fallback"));
        builder.append("\n\n");
        if (isVietnameseLocale()) {
            builder.append("Chào ").append(currentUsername).append(",\n\n");
        } else {
            builder.append("Hi ").append(currentUsername).append(",\n\n");
        }
        builder.append(localized("dashboard.ai.fallback.summary")).append(":\n");
        builder.append("- ")
                .append(isVietnameseLocale() ? "Thu nhập" : "Income")
                .append(": ")
                .append(dashboard.getDashboardOverview().getTotalIncome())
                .append("\n");
        builder.append("- ")
                .append(isVietnameseLocale() ? "Chi tiêu" : "Expense")
                .append(": ")
                .append(dashboard.getDashboardOverview().getTotalExpense())
                .append("\n");
        builder.append("- ")
                .append(isVietnameseLocale() ? "Số dư" : "Balance")
                .append(": ")
                .append(dashboard.getDashboardOverview().getBalance())
                .append("\n");

        if (dashboard.getInsights() != null && !dashboard.getInsights().isEmpty()) {
            builder.append("\n")
                    .append(localized("dashboard.ai.prompt.insights"))
                    .append(":\n");
            dashboard.getInsights().stream().limit(3).forEach(insight ->
                    builder.append("- ").append(insight.getMessage()).append("\n")
            );
        }

        return builder.toString().trim();
    }

    private String callModel(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    private String normalizeDashboardAnswer(String answer, String currentUsername) {
        String normalized = answer == null ? "" : answer.trim().replace("\r\n", "\n");

        String expectedGreeting = isVietnameseLocale()
                ? "Chào " + currentUsername + ","
                : "Hi " + currentUsername + ",";

        normalized = normalized
                .replaceFirst("^Chào\\s+Lutaco,?", expectedGreeting)
                .replaceFirst("^Hi\\s+Lutaco,?", expectedGreeting)
                .replaceFirst("^Hello\\s+Lutaco,?", expectedGreeting);

        if (!normalized.startsWith(expectedGreeting)) {
            normalized = expectedGreeting + "\n\n" + normalized;
        }

        if (!normalized.contains("1.")) {
            String actionHeader = isVietnameseLocale()
                    ? "\n\n1. Hành động ưu tiên 1\n2. Hành động ưu tiên 2\n3. Hành động ưu tiên 3"
                    : "\n\n1. Priority action 1\n2. Priority action 2\n3. Priority action 3";
            normalized = normalized + actionHeader;
        }

        return normalized.trim();
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

    private String localized(String key, Object... args) {
        return localizationUtils.getLocalizedMessage(key, args);
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

    private String sanitizeDisplayName(String rawName) {
        if (rawName == null) {
            return isVietnameseLocale() ? "bạn" : "there";
        }

        String name = rawName.trim().replaceAll("\\s+", " ");
        if (name.isBlank()) {
            return isVietnameseLocale() ? "bạn" : "there";
        }

        if (name.length() > 60) {
            name = name.substring(0, 60);
        }

        return name;
    }

    private String stripAccents(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }
}