package vn.id.luannv.lutaco.insight;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.config.InsightThresholdConfig;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardInsightResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {
    InsightThresholdConfig insightConfig;

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

}
