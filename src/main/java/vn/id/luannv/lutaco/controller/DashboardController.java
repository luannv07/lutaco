package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.service.DashboardService;
import vn.id.luannv.lutaco.util.EnumUtils;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Dashboard",
        description = "API for dashboard operations including summary and data exports."
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class DashboardController {

    DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard summary",
            description = "Provides a summary of key metrics for the dashboard."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy dữ liệu dashboard thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<DashboardResponse>> summary(@RequestParam(defaultValue = "LAST_1_MONTH", required = false) String period) {
        PeriodRange periodRange = EnumUtils.from(PeriodRange.class, period);
        return ResponseEntity.ok(
                BaseResponse.success(
                        dashboardService.handleSummary(periodRange),
                        "Lấy dữ liệu dashboard thành công."
                )
        );
    }

    @Operation(
            summary = "Export basic data to Excel",
            description = "Exports a basic report of dashboard data to an Excel file."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xuất file thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/export/basic")
    public void exportBasic(
            HttpServletResponse response
    ) {
        PeriodRange period = PeriodRange.LAST_1_MONTH;

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xuất file thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Yêu cầu quyền Premium")
    })
    @GetMapping("/export/advanced")
    @PreAuthorize("isAuthenticated() and @securityPermission.isActive() and @securityPermission.isPremiumUser()")
    public void exportAdvanced(
            HttpServletResponse response,
            @RequestParam(defaultValue = "LAST_1_MONTH") String period
    ) {
        PeriodRange periodRange = EnumUtils.from(PeriodRange.class, period);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=advanced-report.xlsx"
        );

        dashboardService.exportAdvanced(response, periodRange);
    }
}
