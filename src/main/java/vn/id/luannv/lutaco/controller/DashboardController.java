package vn.id.luannv.lutaco.controller;

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
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class DashboardController {

    DashboardService dashboardService;

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
