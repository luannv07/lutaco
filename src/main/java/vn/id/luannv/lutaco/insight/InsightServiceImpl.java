package vn.id.luannv.lutaco.insight;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.config.InsightThresholdConfig;
import vn.id.luannv.lutaco.dto.InsightDto;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.util.CustomizeNumberUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static vn.id.luannv.lutaco.service.impl.DashboardServiceImpl.bigDecimalScale;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {
    InsightThresholdConfig insightConfig;
    @Override
    public List<InsightDto> generate(InsightContext ctx) {
        List<InsightDto> insights = new ArrayList<>();

        expenseInsight(ctx.getCurrentExpense(), ctx.getPreviousExpense()).ifPresent(insights::add);
        incomeInsight(ctx.getCurrentIncome(), ctx.getPreviousIncome()).ifPresent(insights::add);
        balanceInsight(ctx.getBalance()).ifPresent(insights::add);
        categoryInsight(ctx.getCategories()).ifPresent(insights::add);

        return insights;
    }
    private InsightDto buildInsight(
            InsightDto.InsightLevel level,
            InsightDto.InsightCode code,
            Double value,
            String unit
    ) {
        return InsightDto.builder()
                .level(level)
                .code(code)
                .value(CustomizeNumberUtils.formatDecimal(value, bigDecimalScale).multiply(BigDecimal.valueOf(100)).doubleValue())
                .unit(unit)
                .colorTone(level.getColorTone())
                .defaultColor(level.getColor())
                .build();
    }
    private Optional<InsightDto> expenseInsight(Long thisMonth, Long lastMonth) {
        if (lastMonth == null || lastMonth == 0) return Optional.empty();

        double rate = (double) (thisMonth - lastMonth) / lastMonth;

        if (rate >= insightConfig.getExpense().getDangerRate()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.DANGER,
                    InsightDto.InsightCode.EXPENSE_INCREASE,
                    rate,
                    "%"
            ));
        }

        if (rate >= insightConfig.getExpense().getWarnRate()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.WARN,
                    InsightDto.InsightCode.EXPENSE_INCREASE,
                    rate,
                    "%"
            ));
        }

        if (rate < 0) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.SUCCESS,
                    InsightDto.InsightCode.EXPENSE_DECREASE,
                    Math.abs(rate),
                    "%"
            ));
        }

        return Optional.empty();
    }
    private Optional<InsightDto> incomeInsight(Long thisMonth, Long lastMonth) {
        if (lastMonth == null || lastMonth == 0) return Optional.empty();

        double rate = (double) (thisMonth - lastMonth) / lastMonth;

        if (rate >= insightConfig.getIncome().getSuccessRate()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.SUCCESS,
                    InsightDto.InsightCode.INCOME_INCREASE,
                    rate,
                    "%"
            ));
        }

        if (rate < 0) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.WARN,
                    InsightDto.InsightCode.INCOME_DECREASE,
                    Math.abs(rate),
                    "%"
            ));
        }

        return Optional.empty();
    }
    private Optional<InsightDto> balanceInsight(Long balance) {
        if (balance < insightConfig.getBalance().getNegative()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.DANGER,
                    InsightDto.InsightCode.NEGATIVE_BALANCE,
                    balance.doubleValue(),
                    "VND"
            ));
        }
        return Optional.empty();
    }
    private Optional<InsightDto> categoryInsight(List<CategoryExpenseResponse> categories) {
        return categories.stream()
                .max(Comparator.comparing(CategoryExpenseResponse::getRatioNormalized))
                .filter(c -> c.getRatioNormalized() >= insightConfig.getCategory().getWarnRatio())
                .map(c -> buildInsight(
                        c.getRatioNormalized() >= insightConfig.getCategory().getDangerRatio()
                                ? InsightDto.InsightLevel.DANGER
                                : InsightDto.InsightLevel.WARN,
                        InsightDto.InsightCode.CATEGORY_DOMINANT,
                        c.getRatioNormalized(),
                        "%"
                ));
    }
}
