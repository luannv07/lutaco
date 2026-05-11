package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.service.DashboardService;
import vn.id.luannv.lutaco.service.GeminiService;
import vn.id.luannv.lutaco.util.EnumUtils;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class DashboardController {

    DashboardService dashboardService;
    GeminiService geminiService;

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

    @GetMapping("/summary/ai")
    public ResponseEntity<BaseResponse<String>> summaryWithAi(
            @RequestParam(defaultValue = "LAST_1_MONTH", required = false) String period,
            @RequestParam(defaultValue = "Hay tom tat dashboard va dua ra 3 khuyen nghi uu tien")
            @NotBlank(message = "{validation.required}")
            @Size(max = 500, message = "{validation.field.too_long}")
            String question
    ) {
        PeriodRange periodRange = EnumUtils.from(PeriodRange.class, period);
        DashboardResponse dashboard = dashboardService.handleSummary(periodRange);

        return ResponseEntity.ok(
                BaseResponse.success(
                        geminiService.askDashboard(question, dashboard),
                        "Phan tich AI dashboard thanh cong."
                )
        );
    }

    @GetMapping("/export/basic")
    public void exportBasic(
            HttpServletResponse response
    ) {
        PeriodRange period = PeriodRange.LAST_1_MONTH;

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=basic-report.csv"
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

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=advanced-report.csv"
        );

        dashboardService.exportAdvanced(response, periodRange);
    }
}
