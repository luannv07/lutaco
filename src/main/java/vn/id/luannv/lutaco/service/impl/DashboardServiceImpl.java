package vn.id.luannv.lutaco.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.InsightDto;
import vn.id.luannv.lutaco.dto.PeriodWindow;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.dto.response.WalletSummaryResponse;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
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
import java.util.TimeZone;

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
        List<WalletSummaryResponse> wallets =getWallets(currentUserId);

        DashboardResponse.DashboardOverview dashboardOverview = buildDashboardOverview(wallets, currentUserId);
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
                .balance(dashboardOverview.getBalance())
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
                                .totalIncome(dashboardOverview.getTotalIncome())
                                .totalExpense(dashboardOverview.getTotalExpense())
                                .balance(insightContext.getBalance())
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
    private List<WalletSummaryResponse> getWallets(String userId) {
        return walletRepository
                .findByUser_Id(userId)
                .stream().map(wallet -> WalletSummaryResponse.builder()
                        .walletName(wallet.getWalletName())
                        .balance(wallet.getCurrentBalance())
                        .build())
                .toList();
    }
    private DashboardResponse.DashboardOverview buildDashboardOverview(List<WalletSummaryResponse> wallets, String username) {
        Long totalIncome = transactionRepository
                .sumAmountByUser(username,
                        CategoryType.INCOME,
                        DateTimeUtils.convertSafeDate(null, false),
                        DateTimeUtils.convertSafeDate(null, true));
        Long totalExpense = transactionRepository
                .sumAmountByUser(username,
                        CategoryType.EXPENSE,
                        DateTimeUtils.convertSafeDate(null, false),
                        DateTimeUtils.convertSafeDate(null, true));
        Long balance = Optional
                .of(wallets.stream().mapToLong(WalletSummaryResponse::getBalance).sum())
                .orElse(0L);

        return DashboardResponse.DashboardOverview.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }
    @Override
    public void exportBasic(HttpServletResponse response, PeriodRange period) {
        PeriodWindow window = PeriodWindowFactory.of(period);
        // info
        LocalDateTime now = LocalDateTime.now();
        String author = SecurityUtils.getCurrentUsername();
        String authorId = SecurityUtils.getCurrentId();
        String from = DateTimeUtils.format(window.getFrom(), "dd/MM/yyyy");
        String to = DateTimeUtils.format(window.getTo(), "dd/MM/yyyy");
        String exportedAt = DateTimeUtils.format(now, "dd/MM/yyyy HH:mm:ss");
        List<WalletSummaryResponse> wallets = getWallets(authorId);
        DashboardResponse.DashboardOverview dashboardOverview = buildDashboardOverview(wallets, authorId);
        // info
        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Overview");

            /* ===== Styles ===== */
            HSSFCellStyle boldStyle = workbook.createCellStyle();
            HSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            HSSFCellStyle centerBold = workbook.createCellStyle();
            centerBold.setFont(boldFont);
            centerBold.setAlignment(HorizontalAlignment.CENTER);

            HSSFCellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            HSSFCellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(
                    workbook.createDataFormat().getFormat("#,##0")
            );
            moneyStyle.setAlignment(HorizontalAlignment.CENTER);

            /* ===== Row 0–2: Metadata ===== */
            int rowIdx = 0;

            HSSFRow r0 = sheet.createRow(rowIdx++);
            r0.createCell(0).setCellValue("Author:");
            r0.createCell(1).setCellValue(author);
            r0.getCell(0).setCellStyle(boldStyle);

            HSSFRow r1 = sheet.createRow(rowIdx++);
            r1.createCell(0).setCellValue("Period:");
            r1.createCell(1).setCellValue(from + " - " + to);
            r1.getCell(0).setCellStyle(boldStyle);

            HSSFRow r2 = sheet.createRow(rowIdx++);
            r2.createCell(0).setCellValue("Exported at:");
            r2.createCell(1).setCellValue(exportedAt);
            r2.getCell(0).setCellStyle(boldStyle);

            /* ===== Blank row ===== */
            rowIdx++;

            /* ===== Title ===== */
            HSSFRow titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("OVERVIEW");
            titleRow.getCell(0).setCellStyle(centerBold);
            sheet.addMergedRegion(new CellRangeAddress(
                    titleRow.getRowNum(),
                    titleRow.getRowNum(),
                    0, 3
            ));

            /* ===== Blank row ===== */
            rowIdx++;

            /* ===== Header ===== */
            HSSFRow header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Total income");
            header.createCell(1).setCellValue("Total expense");
            header.createCell(2).setCellValue("Current balance");
            header.createCell(3).setCellValue("Currency");

            for (int i = 0; i < 4; i++) {
                header.getCell(i).setCellStyle(centerBold);
            }

            /* ===== Values ===== */
            HSSFRow values = sheet.createRow(rowIdx++);
            values.createCell(0).setCellValue(dashboardOverview.getTotalIncome().doubleValue());
            values.createCell(1).setCellValue(dashboardOverview.getTotalExpense().doubleValue());
            values.createCell(2).setCellValue(dashboardOverview.getBalance().doubleValue());
            values.createCell(3).setCellValue("VND");

            for (int i = 0; i < 4; i++) {
                values.getCell(i).setCellStyle(moneyStyle);
                sheet.autoSizeColumn(i);
            }

            /* ===== Write ===== */
            workbook.write(response.getOutputStream());
            workbook.close();
            response.flushBuffer();
            log.info("BasicReport: success export.");
        } catch (Exception e) {
            log.info("BasicReport: failed export.");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
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
