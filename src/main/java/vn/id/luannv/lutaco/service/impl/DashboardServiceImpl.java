package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.InsightDto;
import vn.id.luannv.lutaco.dto.PeriodWindow;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.dto.response.WalletSummaryResponse;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.insight.InsightContext;
import vn.id.luannv.lutaco.insight.InsightService;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.DashboardService;
import vn.id.luannv.lutaco.util.CustomizeNumberUtils;
import vn.id.luannv.lutaco.util.DateTimeUtils;
import vn.id.luannv.lutaco.util.PeriodWindowFactory;
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
    public DashboardResponse handleSummary(PeriodRange range) {
        // top
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
        // end top
        PeriodWindow window = PeriodWindowFactory.of(range);

        Long currentIncome = Optional.ofNullable(
                transactionRepository.sumAmountByUser(
                        currentUserId,
                        CategoryType.INCOME,
                        window.getFrom(),
                        window.getTo()
                )
        ).orElse(0L);

        Long currentExpense = Optional.ofNullable(
                transactionRepository.sumAmountByUser(
                        currentUserId,
                        CategoryType.EXPENSE,
                        window.getFrom(),
                        window.getTo()
                )
        ).orElse(0L);

        Long previousIncome = Optional.ofNullable(
                transactionRepository.sumAmountByUser(
                        currentUserId,
                        CategoryType.INCOME,
                        window.getPreviousFrom(),
                        window.getPreviousTo()
                )
        ).orElse(0L);

        Long previousExpense = Optional.ofNullable(
                transactionRepository.sumAmountByUser(
                        currentUserId,
                        CategoryType.EXPENSE,
                        window.getPreviousFrom(),
                        window.getPreviousTo()
                )
        ).orElse(0L);

        List<CategoryExpenseResponse> expenseCategories =
                genCategoryChartInfoBetweenDate(window.getFrom(), window.getTo());

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
                .currentIncome(currentIncome)
                .currentExpense(currentExpense)
                .previousExpense(previousExpense)
                .previousIncome(previousIncome)
                .balance(balance)
                .categories(expenseCategories)
                .build();

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

        DashboardResponse.PeriodComparison period =
                DashboardResponse.PeriodComparison.builder()
                        .current(current)
                        .previous(previous)
                        .growthRate(growthRate)
                        .build();

        List<InsightDto> dto = insightService.generate(insightContext);
        return DashboardResponse.builder()
                .dashboardOverview(
                        DashboardResponse.DashboardOverview.builder()
                                .totalIncome(totalIncome)
                                .totalExpense(totalExpense)
                                .balance(balance)
                                .build()
                )
                .walletSummary(
                        DashboardResponse.WalletSummary.builder()
                                .wallets(wallets)
                                .walletCount(wallets.size())
                                .build()
                )
                .topExpenseCategories(insightContext.getCategories())
                .insight(dto)
                .period(period)
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
