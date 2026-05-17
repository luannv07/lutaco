package vn.id.luannv.lutaco.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.PeriodWindow;
import vn.id.luannv.lutaco.dto.projection.CategoryAmountProjection;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.dto.response.WalletSummaryResponse;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.insight.InsightContext;
import vn.id.luannv.lutaco.insight.InsightService;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.DashboardService;
import vn.id.luannv.lutaco.util.PeriodWindowFactory;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardServiceImpl implements DashboardService {

    TransactionRepository transactionRepository;
    WalletRepository walletRepository;
    InsightService insightService;

    @Override
    public DashboardResponse handleSummary(PeriodRange range) {
        Long userId = SecurityUtils.getCurrentId();
        PeriodWindow window = PeriodWindowFactory.of(range);

        Instant currentFrom = toUtcInstant(window.getFrom());
        Instant currentTo = toUtcInstant(window.getTo());
        Instant previousFrom = toUtcInstant(window.getPreviousFrom());
        Instant previousTo = toUtcInstant(window.getPreviousTo());

        Long currentIncome = aggregateByType(userId, CategoryType.INCOME, currentFrom, currentTo);
        Long currentExpense = aggregateByType(userId, CategoryType.EXPENSE, currentFrom, currentTo);
        Long previousIncome = aggregateByType(userId, CategoryType.INCOME, previousFrom, previousTo);
        Long previousExpense = aggregateByType(userId, CategoryType.EXPENSE, previousFrom, previousTo);

        List<Wallet> wallets = walletRepository.findByUserIdAndActiveFlgTrue(userId);
        Long balance = wallets.stream().map(Wallet::getBalance).reduce(0L, Long::sum);

        List<CategoryExpenseResponse> topExpenseCategories = transactionRepository
                .findTopExpenseCategoriesByUserAndDateRange(userId, currentFrom, currentTo, PageRequest.of(0, 5))
                .stream()
                .map(category -> toCategoryExpense(category, currentExpense))
                .toList();

        DashboardResponse.PeriodSummary current = DashboardResponse.PeriodSummary.builder()
                .from(window.getFrom())
                .to(window.getTo())
                .income(currentIncome)
                .expense(currentExpense)
                .build();

        DashboardResponse.PeriodSummary previous = DashboardResponse.PeriodSummary.builder()
                .from(window.getPreviousFrom())
                .to(window.getPreviousTo())
                .income(previousIncome)
                .expense(previousExpense)
                .build();

        DashboardResponse.GrowthRate growthRate = DashboardResponse.GrowthRate.builder()
                .income(growthRate(currentIncome, previousIncome))
                .expense(growthRate(currentExpense, previousExpense))
                .build();

        InsightContext insightContext = InsightContext.builder()
                .currentIncome(currentIncome)
                .currentExpense(currentExpense)
                .previousIncome(previousIncome)
                .previousExpense(previousExpense)
                .balance(balance)
                .categories(topExpenseCategories)
                .build();

        return DashboardResponse.builder()
                .dashboardOverview(DashboardResponse.DashboardOverview.builder()
                        .totalIncome(currentIncome)
                        .totalExpense(currentExpense)
                        .balance(balance)
                        .build())
                .walletSummary(DashboardResponse.WalletSummary.builder()
                        .walletCount(wallets.size())
                        .wallets(wallets.stream()
                                .map(wallet -> WalletSummaryResponse.builder()
                                        .walletName(wallet.getName())
                                        .balance(wallet.getBalance())
                                        .build())
                                .toList())
                        .build())
                .topExpenseCategories(topExpenseCategories)
                .period(DashboardResponse.PeriodComparison.builder()
                        .current(current)
                        .previous(previous)
                        .growthRate(growthRate)
                        .build())
                .insights(insightService.generate(insightContext))
                .build();
    }

    @Override
    public void exportBasic(HttpServletResponse response, PeriodRange period) {
        DashboardResponse summary = handleSummary(period);
        StringBuilder csv = new StringBuilder();
        csv.append("metric,value\n");
        csv.append("totalIncome,").append(summary.getDashboardOverview().getTotalIncome()).append("\n");
        csv.append("totalExpense,").append(summary.getDashboardOverview().getTotalExpense()).append("\n");
        csv.append("balance,").append(summary.getDashboardOverview().getBalance()).append("\n");

        writeCsv(response, csv.toString());
    }

    @Override
    public void exportAdvanced(HttpServletResponse response, PeriodRange period) {
        DashboardResponse summary = handleSummary(period);
        StringBuilder csv = new StringBuilder();

        csv.append("section,key,value\n");
        csv.append("overview,totalIncome,").append(summary.getDashboardOverview().getTotalIncome()).append("\n");
        csv.append("overview,totalExpense,").append(summary.getDashboardOverview().getTotalExpense()).append("\n");
        csv.append("overview,balance,").append(summary.getDashboardOverview().getBalance()).append("\n");

        summary.getTopExpenseCategories().forEach(category ->
                csv.append("topExpense,")
                        .append(category.getCategoryName()).append(",")
                        .append(category.getAmount()).append(" (")
                        .append(String.format("%.2f", category.getRatioNormalized())).append("%)\n")
        );

        summary.getInsights().forEach(insight ->
                csv.append("insight,")
                        .append(insight.getCodeCd()).append(",")
                        .append(insight.getMessage().replace(",", " ")).append("\n")
        );

        writeCsv(response, csv.toString());
    }

    private Long aggregateByType(Long userId, CategoryType categoryType, Instant from, Instant to) {
        Long value = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, categoryType, from, to);
        return value == null ? 0L : value;
    }

    private CategoryExpenseResponse toCategoryExpense(CategoryAmountProjection projection, Long totalExpense) {
        long safeTotal = totalExpense == null ? 0L : totalExpense;
        double ratio = safeTotal == 0 ? 0D : (projection.getTotal() * 100D) / safeTotal;
        return CategoryExpenseResponse.builder()
                .categoryName(projection.getCategoryName())
                .amount(projection.getTotal())
                .ratioNormalized(ratio)
                .build();
    }

    private Double growthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return 0D;
        }
        return ((current - previous) * 100D) / previous;
    }

    private Instant toUtcInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    private void writeCsv(HttpServletResponse response, String content) {
        try {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(content);
            response.getWriter().flush();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot export dashboard report", e);
        }
    }
}
