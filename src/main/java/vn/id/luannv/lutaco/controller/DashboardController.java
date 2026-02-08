package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.service.DashboardService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Dashboard API",
        description = "API for dashboard operations including summary and data exports."
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class DashboardController {

    DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard summary",
            description = "Provides a summary of key metrics for the dashboard."
    )
    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<DashboardResponse>> summary(@RequestParam(defaultValue = "THIS_MONTH", required = false) String range) {
        PeriodRange period = PeriodRange.from(range);
        return ResponseEntity.ok(
                BaseResponse.success(
                        dashboardService.handleSummary(period),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @Operation(
            summary = "Export basic data to Excel",
            description = "Exports a basic report of dashboard data to an Excel file."
    )
    @GetMapping("/export/basic")
    public void exportBasic(
            HttpServletResponse response,
            @RequestParam(defaultValue = "THIS_MONTH") String range
    ) {
        PeriodRange period = PeriodRange.from(range);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=basic-report.xlsx"
        );

        dashboardService.exportBasic(response, period);
    }

    @Operation(
            summary = "Export advanced data to Excel (Premium)",
            description = "Exports a more detailed report of dashboard data to an Excel file. This feature is available only to premium users."
    )
    @GetMapping("/export/advanced")
//    @PreAuthorize("@securityPermission.isPremiumUser()")
    public void exportAdvanced(
            HttpServletResponse response,
            @RequestParam(defaultValue = "THIS_MONTH") String range
    ) {
        PeriodRange period = PeriodRange.from(range);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=advanced-report.xlsx"
        );

        dashboardService.exportAdvanced(response, period);
    }
}
