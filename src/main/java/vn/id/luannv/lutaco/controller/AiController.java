package vn.id.luannv.lutaco.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.service.DashboardService;
import vn.id.luannv.lutaco.service.GeminiService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class AiController {

    GeminiService geminiService;
    DashboardService dashboardService;

    @GetMapping("/chat")
    public ResponseEntity<BaseResponse<String>> chat(
            @RequestParam
            @NotBlank(message = "{validation.required}")
            @Size(max = 500, message = "{validation.field.too_long}")
            String message
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(geminiService.askGemini(message), "AI tra loi thanh cong.")
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<BaseResponse<String>> dashboardInsight(
            @RequestParam(defaultValue = "LAST_1_MONTH") String period,
            @RequestParam(defaultValue = "Hay tom tat dashboard va dua ra 3 khuyen nghi uu tien")
            @NotBlank(message = "{validation.required}")
            @Size(max = 500, message = "{validation.field.too_long}")
            String question
    ) {
        PeriodRange periodRange = EnumUtils.from(PeriodRange.class, period);
        DashboardResponse summary = dashboardService.handleSummary(periodRange);

        return ResponseEntity.ok(
                BaseResponse.success(
                        geminiService.askDashboard(question, summary, SecurityUtils.getCurrentUsername()),
                        "AI phan tich dashboard thanh cong."
                )
        );
    }
}