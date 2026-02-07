package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.InsightDto;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.dto.response.WalletSummaryResponse;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.insight.InsightContext;
import vn.id.luannv.lutaco.insight.InsightService;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.DashboardService;
import vn.id.luannv.lutaco.util.CustomizeNumberUtils;
import vn.id.luannv.lutaco.util.DateTimeUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardServiceImpl implements DashboardService {
    WalletRepository walletRepository;
    TransactionRepository transactionRepository;
    InsightService insightService;

    public static int bigDecimalScale = 4;

    @Override
    @Transactional
    public DashboardResponse handleSummary() {
        String currentUserId = SecurityUtils.getCurrentId();
        List<WalletSummaryResponse> wallets = walletRepository
                .findByUser_Id(currentUserId)
                .stream().map(wallet -> WalletSummaryResponse.builder()
                        .walletName(wallet.getWalletName())
                        .balance(wallet.getCurrentBalance())
                        .build())
                .toList();
        Long totalIncome = transactionRepository
                .sumAmountByUser(currentUserId,
                        CategoryType.INCOME,
                        DateTimeUtils.convertSafeDate(null, false),
                        DateTimeUtils.convertSafeDate(null, true));
        Long totalExpense = transactionRepository
                .sumAmountByUser(currentUserId,
                        CategoryType.EXPENSE,
                        DateTimeUtils.convertSafeDate(null, false),
                        DateTimeUtils.convertSafeDate(null, true));
        Long balance = Optional
                .of(wallets.stream().mapToLong(WalletSummaryResponse::getBalance).sum())
                .orElse(0L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startThisMonth = now.withDayOfMonth(1);
        LocalDateTime startLastMonth = now
                .withDayOfMonth(1)
                .minusMonths(1);
        LocalDateTime endLastMonth = now
                .withDayOfMonth(1)
                .minusDays(1);
        Long totalIncomeThisMonth = Optional.ofNullable(transactionRepository
                .sumAmountByUser(currentUserId,
                        CategoryType.INCOME,
                        startThisMonth,
                        now)).orElse(0L);
        Long totalIncomeLastMonth = Optional.ofNullable(transactionRepository
                .sumAmountByUser(currentUserId,
                        CategoryType.INCOME,
                        startLastMonth,
                        endLastMonth)).orElse(0L);
        Long totalExpenseThisMonth = Optional.ofNullable(transactionRepository
                .sumAmountByUser(currentUserId,
                        CategoryType.EXPENSE,
                        startThisMonth,
                        now)).orElse(0L);
        Long totalExpenseLastMonth = Optional.ofNullable(transactionRepository
                .sumAmountByUser(currentUserId,
                        CategoryType.EXPENSE,
                        startLastMonth,
                        endLastMonth)).orElse(0L);

        List<CategoryExpenseResponse> expenseCategories =
                genCategoryChartInfoBetweenDate(startLastMonth, now);

        BigDecimal sum = expenseCategories.stream()
                .map(c -> BigDecimal.valueOf(c.getRatioNormalized()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal diff = BigDecimal.ONE.subtract(sum);

        if (!expenseCategories.isEmpty() && diff.compareTo(BigDecimal.ZERO) != 0) {
            CategoryExpenseResponse first = expenseCategories.get(0);
            first.setRatioNormalized(
                    BigDecimal.valueOf(first.getRatioNormalized())
                            .add(diff)
                            .doubleValue()
            );
        }
        InsightContext insightContext = InsightContext.builder()
                .incomeThisMonth(totalIncomeThisMonth)
                .incomeLastMonth(totalIncomeLastMonth)
                .expenseLastMonth(totalExpenseLastMonth)
                .expenseThisMonth(totalExpenseThisMonth)
                .balance(balance)
                .categories(expenseCategories)
                .build();

        List<InsightDto> dto = insightService.generate(insightContext);
        return DashboardResponse.builder()
                .dashboardOverview(DashboardResponse.DashboardOverview.builder()
                        .totalIncome(totalIncome)
                        .totalExpense(totalExpense)
                        .balance(balance)
                        .build())
                .insight(dto)
                .walletSummary(DashboardResponse.WalletSummary.builder()
                        .wallets(wallets)
                        .walletCount(wallets.size())
                        .build())
                .topExpenseCategories(insightContext.getCategories())
                .thisMonth(DashboardResponse.MonthSummary.builder()
                        .income(totalIncomeThisMonth)
                        .expense(totalExpenseThisMonth)
                        .build())
                .lastMonth(DashboardResponse.MonthSummary.builder()
                        .income(totalIncomeLastMonth)
                        .expense(totalExpenseLastMonth)
                        .build())
                .growthRate(DashboardResponse.GrowthRate.builder()
                        .income(growthRate(totalIncomeThisMonth, totalIncomeLastMonth))
                        .expense(growthRate(totalExpenseThisMonth, totalExpenseLastMonth))
                        .build())
                .build();
    }
    private List<CategoryExpenseResponse> genCategoryChartInfoBetweenDate(LocalDateTime from, LocalDateTime to) {
        return transactionRepository
                .getCategoryPercentageOfTotal(SecurityUtils.getCurrentId(),
                        CategoryType.EXPENSE.name(),
                        DateTimeUtils.convertSafeDate(from, false),
                        DateTimeUtils.convertSafeDate(to, true))
                .stream()
                .map(cep ->
                        CategoryExpenseResponse.builder()
                                .categoryName(cep.getCategoryParentName())
                                .amount(cep.getTotal())
                                .ratioNormalized(
                                        BigDecimal.valueOf(cep.getPct())
                                                .setScale(bigDecimalScale, RoundingMode.HALF_UP)
                                                .doubleValue()
                                )
                                .build()
                )
                .toList();
    }
    @Override
    public void exportBasic() {

    }

    @Override
    public void exportAdvanced() {

    }

    private Double growthRate(long current, long previous) {
        if (previous == 0) {
            if (current == 0) return 0.0;
            return 100.0;
        }
        return CustomizeNumberUtils
                .formatDecimal((current * 1.0 - previous) / previous, bigDecimalScale)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
