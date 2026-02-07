package vn.id.luannv.lutaco.dto.response;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.dto.InsightDto;

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

    MonthSummary thisMonth;
    MonthSummary lastMonth;

    GrowthRate growthRate;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MonthSummary {
        Long income;
        Long expense;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GrowthRate {
        @DecimalMin(message = "dashboard.message.invalidConfig", value = "0.0")
        @DecimalMax(message = "dashboard.message.invalidConfig", value = "100.0")
        Double income;
        @DecimalMin(message = "dashboard.message.invalidConfig", value = "0.0")
        @DecimalMax(message = "dashboard.message.invalidConfig", value = "100.0")
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
