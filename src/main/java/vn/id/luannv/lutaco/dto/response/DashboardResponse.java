package vn.id.luannv.lutaco.dto.response;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.dto.InsightDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardResponse {
    DashboardOverview dashboardOverview;
    List<InsightDto> insight;

    WalletSummary walletSummary;

    List<CategoryExpenseResponse> topExpenseCategories;

    PeriodComparison period;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PeriodComparison {

        PeriodSummary current;
        PeriodSummary previous;
        GrowthRate growthRate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PeriodSummary {

        LocalDateTime from;    // yyyy-MM-dd
        LocalDateTime to;      // yyyy-MM-dd

        Long income;
        Long expense;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GrowthRate {
        Double income;
        Double expense;
        @Builder.Default
        String unit = "%";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DashboardOverview {
        Long totalIncome;
        Long totalExpense;
        Long balance;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WalletSummary {
        Integer walletCount;
        List<WalletSummaryResponse> wallets;
    }
}
