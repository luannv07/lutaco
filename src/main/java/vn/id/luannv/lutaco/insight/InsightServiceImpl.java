package vn.id.luannv.lutaco.insight;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.config.InsightThresholdConfig;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardInsightResponse;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Locale;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {
    InsightThresholdConfig insightConfig;
    LocalizationUtils localizationUtils;

    @Override
    public List<DashboardInsightResponse> generate(InsightContext ctx) {
        List<DashboardInsightResponse> insights = new ArrayList<>();

        expenseInsight(ctx.getCurrentExpense(), ctx.getPreviousExpense()).ifPresent(insights::add);
        incomeInsight(ctx.getCurrentIncome(), ctx.getPreviousIncome()).ifPresent(insights::add);
        balanceInsight(ctx.getBalance()).ifPresent(insights::add);
        categoryInsight(ctx.getCategories()).ifPresent(insights::add);

        return insights;
    }

    private DashboardInsightResponse buildInsight(
            DashboardInsightResponse.InsightLevel level,
            DashboardInsightResponse.InsightCode code,
            Double value,
            String unit
    ) {
        return DashboardInsightResponse.builder()
                .levelCd(level)
                .codeCd(code)
                .value(value)
                .unit(unit)
                .color(resolveColor(level))
                .message(resolveMessage(code, value))
                .recommendation(resolveRecommendation(code, level))
                .build();
    }

    private Optional<DashboardInsightResponse> expenseInsight(Long thisMonth, Long lastMonth) {
        if (lastMonth == null || lastMonth == 0 || thisMonth == null) return Optional.empty();

        double rate = (double) (thisMonth - lastMonth) / lastMonth;

        if (rate >= insightConfig.getExpense().getDangerRate()) {
            return Optional.ofNullable(buildInsight(
                    DashboardInsightResponse.InsightLevel.DANGER,
                    DashboardInsightResponse.InsightCode.EXPENSE_INCREASE,
                    rate * 100,
                    "%"
            ));
        }

        if (rate >= insightConfig.getExpense().getWarnRate()) {
            return Optional.ofNullable(buildInsight(
                    DashboardInsightResponse.InsightLevel.WARN,
                    DashboardInsightResponse.InsightCode.EXPENSE_INCREASE,
                    rate * 100,
                    "%"
            ));
        }

        if (rate < 0) {
            return Optional.ofNullable(buildInsight(
                    DashboardInsightResponse.InsightLevel.SUCCESS,
                    DashboardInsightResponse.InsightCode.EXPENSE_DECREASE,
                    Math.abs(rate) * 100,
                    "%"
            ));
        }

        return Optional.empty();
    }

    private Optional<DashboardInsightResponse> incomeInsight(Long thisMonth, Long lastMonth) {
        if (lastMonth == null || lastMonth == 0 || thisMonth == null) return Optional.empty();

        double rate = (double) (thisMonth - lastMonth) / lastMonth;

        if (rate >= insightConfig.getIncome().getSuccessRate()) {
            return Optional.ofNullable(buildInsight(
                    DashboardInsightResponse.InsightLevel.SUCCESS,
                    DashboardInsightResponse.InsightCode.INCOME_INCREASE,
                    rate * 100,
                    "%"
            ));
        }

        if (rate < 0) {
            return Optional.ofNullable(buildInsight(
                    DashboardInsightResponse.InsightLevel.WARN,
                    DashboardInsightResponse.InsightCode.INCOME_DECREASE,
                    Math.abs(rate) * 100,
                    "%"
            ));
        }

        return Optional.empty();
    }

    private Optional<DashboardInsightResponse> balanceInsight(Long balance) {
        if (balance == null) {
            return Optional.empty();
        }

        if (balance < insightConfig.getBalance().getNegative()) {
            return Optional.ofNullable(buildInsight(
                    DashboardInsightResponse.InsightLevel.DANGER,
                    DashboardInsightResponse.InsightCode.NEGATIVE_BALANCE,
                    balance.doubleValue(),
                    "VND"
            ));
        }
        return Optional.empty();
    }

    private Optional<DashboardInsightResponse> categoryInsight(List<CategoryExpenseResponse> categories) {
        if (categories == null || categories.isEmpty()) {
            return Optional.empty();
        }

        return categories.stream()
                .max(Comparator.comparing(CategoryExpenseResponse::getRatioNormalized))
                .filter(c -> normalizePercentToRatio(c.getRatioNormalized()) >= insightConfig.getCategory().getWarnRatio())
                .map(c -> buildInsight(
                        normalizePercentToRatio(c.getRatioNormalized()) >= insightConfig.getCategory().getDangerRatio()
                                ? DashboardInsightResponse.InsightLevel.DANGER
                                : DashboardInsightResponse.InsightLevel.WARN,
                        DashboardInsightResponse.InsightCode.CATEGORY_DOMINANT,
                        c.getRatioNormalized(),
                        "%"
                ));
    }

    private double normalizePercentToRatio(Double percent) {
        if (percent == null) {
            return 0D;
        }
        if (percent > 1) {
            return percent / 100D;
        }
        return percent;
    }

    private String resolveColor(DashboardInsightResponse.InsightLevel level) {
        return switch (level) {
            case SUCCESS -> "#16a34a";
            case WARN -> "#f59e0b";
            case DANGER -> "#ef4444";
        };
    }

    private String resolveMessage(DashboardInsightResponse.InsightCode code, Double value) {
        String valueText = value == null ? "0" : String.format(Locale.US, "%.2f", value);
        return switch (code) {
            case EXPENSE_INCREASE -> localized(
                    value != null && value >= insightConfig.getExpense().getDangerRate() * 100
                            ? "dashboard.insight.expense.increase.danger"
                            : "dashboard.insight.expense.increase.message",
                    valueText
            );
            case EXPENSE_DECREASE -> localized("dashboard.insight.expense.decrease.message", valueText);
            case INCOME_INCREASE -> localized("dashboard.insight.income.increase.message", valueText);
            case INCOME_DECREASE -> localized("dashboard.insight.income.decrease.message", valueText);
            case NEGATIVE_BALANCE -> localized("dashboard.insight.balance.negative.message", valueText);
            case CATEGORY_DOMINANT -> localized("dashboard.insight.category.dominant.message", valueText);
        };
    }

    private String resolveRecommendation(
            DashboardInsightResponse.InsightCode code,
            DashboardInsightResponse.InsightLevel level
    ) {
        return switch (code) {
            case EXPENSE_INCREASE -> level == DashboardInsightResponse.InsightLevel.DANGER
                    ? localized("dashboard.insight.expense.increase.danger.recommendation")
                    : localized("dashboard.insight.expense.increase.recommendation");
            case EXPENSE_DECREASE -> localized("dashboard.insight.expense.decrease.recommendation");
            case INCOME_INCREASE -> localized("dashboard.insight.income.increase.recommendation");
            case INCOME_DECREASE -> localized("dashboard.insight.income.decrease.recommendation");
            case NEGATIVE_BALANCE -> localized("dashboard.insight.balance.negative.recommendation");
            case CATEGORY_DOMINANT -> localized("dashboard.insight.category.dominant.recommendation");
        };
    }

    private String localized(String key, Object... args) {
        return localizationUtils.getLocalizedMessage(key, args);
    }
}
