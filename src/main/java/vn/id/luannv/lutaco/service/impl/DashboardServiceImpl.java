package vn.id.luannv.lutaco.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
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
import vn.id.luannv.lutaco.export.ExportContainer;
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

import static vn.id.luannv.lutaco.export.ExportContainer.createCommonStyles;
import static vn.id.luannv.lutaco.export.ExportContainer.createMetaRow;

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
        List<WalletSummaryResponse> wallets = getWallets(currentUserId);

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

    @Override
    public void exportBasic(HttpServletResponse response, PeriodRange period) {
        PeriodWindow window = PeriodWindowFactory.of(period);

        ExportContainer.ExportContext ctx = new ExportContainer.ExportContext(
                SecurityUtils.getCurrentUsername(),
                SecurityUtils.getCurrentId(),
                window,
                LocalDateTime.now(),
                period.name()
        );

        DashboardResponse.DashboardOverview overview =
                buildDashboardOverview(getWallets(ctx.authorId()), ctx.authorId());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            ExportContainer.ExcelStyles styles = createCommonStyles(workbook);

            XSSFSheet sheet = workbook.createSheet("Overview");
            buildOverviewSheetXSSF(sheet, styles, ctx, overview);

            workbook.write(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    @Transactional
    public void exportAdvanced(HttpServletResponse response, PeriodRange period) {
        PeriodWindow window = PeriodWindowFactory.of(period);

        ExportContainer.ExportContext ctx = new ExportContainer.ExportContext(
                SecurityUtils.getCurrentUsername(),
                SecurityUtils.getCurrentId(),
                window,
                LocalDateTime.now(),
                period.name()
        );

        DashboardResponse.DashboardOverview overview =
                buildDashboardOverview(getWallets(ctx.authorId()), ctx.authorId());

        List<CategoryExpenseResponse> categories =
                genCategoryChartInfoBetweenDate(window.getFrom(), window.getTo());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            ExportContainer.ExcelStyles styles = createCommonStyles(workbook);

            /* ===== Sheet 1: Overview (reuse basic) ===== */
            buildOverviewSheetXSSF(
                    workbook.createSheet("Overview"),
                    styles,
                    ctx,
                    overview
            );

            /* ===== Sheet 2: Top Expense ===== */
            buildTopExpenseSheetXSSF(
                    workbook.createSheet("Top Expenses"),
                    styles.getCenterBold(),
                    styles.getMoney(),
                    categories
            );

            /* ===== Sheet 3: Pie Chart ===== */
            if (!categories.isEmpty()) {
                buildExpensePieChartXSSF(
                        workbook,
                        workbook.createSheet("Expense Chart"),
                        categories
                );
            }

            workbook.write(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private void buildOverviewSheetXSSF(
            XSSFSheet sheet,
            ExportContainer.ExcelStyles styles,
            ExportContainer.ExportContext ctx,
            DashboardResponse.DashboardOverview overview
    ) {
        int rowIdx = 0;
        rowIdx = ExportContainer.createTitleRow(
                sheet,
                rowIdx,
                "REPORT INFO | TYPE: " + ctx.range(),
                0,
                2,
                styles.getCenterBold()
        );

        String from = DateTimeUtils.format(ctx.window().getFrom(), "dd/MM/yyyy");
        String to = DateTimeUtils.format(ctx.window().getTo(), "dd/MM/yyyy");
        String exportedAt = DateTimeUtils.format(ctx.exportedAt(), "dd/MM/yyyy HH:mm:ss");

        /* ===== Metadata ===== */
        rowIdx = createMetaRow(sheet, rowIdx, "Author:", ctx.author(), styles.getBold());
        rowIdx = createMetaRow(sheet, rowIdx, "Period:", from + " - " + to, styles.getBold());
        rowIdx = createMetaRow(sheet, rowIdx, "Exported at:", exportedAt, styles.getBold());
        rowIdx = createMetaRow(sheet, rowIdx, "Currency: ", "VND", styles.getBold());

        rowIdx++;

        /* ===== Title ===== */
        XSSFRow title = sheet.createRow(rowIdx++);
        title.createCell(0).setCellValue("OVERVIEW");
        title.getCell(0).setCellStyle(styles.getCenterBold());

        sheet.addMergedRegion(new CellRangeAddress(
                title.getRowNum(), title.getRowNum(), 0, 2
        ));

        rowIdx++;

        /* ===== Header ===== */
        XSSFRow header = sheet.createRow(rowIdx++);
        String[] headers = {
                "Total income", "Total expense", "Current balance"
        };

        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(styles.getCenterBold());
        }

        /* ===== Values ===== */
        XSSFRow values = sheet.createRow(rowIdx++);

        values.createCell(0).setCellValue(overview.getTotalIncome().doubleValue());
        values.createCell(1).setCellValue(overview.getTotalExpense().doubleValue());
        values.createCell(2).setCellValue(overview.getBalance().doubleValue());

        for (int i = 0; i < 3; i++) {
            values.getCell(i).setCellStyle(styles.getMoney());
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(3) + 6 * 256);
        }
    }

    private void buildTopExpenseSheetXSSF(XSSFSheet sheet, XSSFCellStyle headerStyle, XSSFCellStyle money, List<CategoryExpenseResponse> categories) {
        int r = 0;

        Row header = sheet.createRow(r++);
        header.createCell(0).setCellValue("Category");
        header.createCell(1).setCellValue("Amount");
        header.createCell(2).setCellValue("Ratio (%)");

        for (int i = 0; i < 3; i++) header.getCell(i).setCellStyle(headerStyle);

        for (CategoryExpenseResponse c : categories) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(c.getCategoryName());
            row.createCell(1).setCellValue(c.getAmount());
            row.createCell(2).setCellValue(c.getRatioNormalized() * 100);

            row.getCell(1).setCellStyle(money);
        }

        for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
    }

    private void buildExpensePieChartXSSF(XSSFWorkbook workbook, XSSFSheet sheet, List<CategoryExpenseResponse> categories) {
        int r = 0;

        // ===== Data table =====
        Row header = sheet.createRow(r++);
        header.createCell(0).setCellValue("Category");
        header.createCell(1).setCellValue("Ratio");

        for (CategoryExpenseResponse c : categories) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(c.getCategoryName());
            row.createCell(1).setCellValue(c.getRatioNormalized());
        }

        // ===== Drawing & chart =====
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(
                0, 0, 0, 0,
                3, 1,
                15, 20
        );

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Expense Distribution");
        chart.setTitleOverlay(false);

        // ===== Legend (XDDF) =====
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        // ===== Data sources =====
        XDDFDataSource<String> categoriesData =
                XDDFDataSourcesFactory.fromStringCellRange(
                        sheet,
                        new CellRangeAddress(1, categories.size(), 0, 0)
                );

        XDDFNumericalDataSource<Double> valuesData =
                XDDFDataSourcesFactory.fromNumericCellRange(
                        sheet,
                        new CellRangeAddress(1, categories.size(), 1, 1)
                );

        // ===== Pie chart =====
        XDDFPieChartData pieData =
                (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);

        XDDFPieChartData.Series series =
                (XDDFPieChartData.Series) pieData.addSeries(categoriesData, valuesData);

        series.setTitle("Expense Ratio", null);

        chart.plot(pieData);
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
}
