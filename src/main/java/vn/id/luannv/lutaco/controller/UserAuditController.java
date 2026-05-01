package vn.id.luannv.lutaco.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.UserAuditFilterRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.entity.UserAuditLog;
import vn.id.luannv.lutaco.service.UserAuditService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/user-audit-logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN') or hasRole('SYS_ADMIN')")
public class UserAuditController {

    UserAuditService userAuditService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<UserAuditLog>>> viewUserAuditLogs(UserAuditFilterRequest filter) {
        return ResponseEntity.ok(BaseResponse.success(userAuditService.viewUserAuditLogs(filter), "Lấy danh sách log thành công."));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteUserAuditLogs(@RequestBody UserAuditFilterRequest filter) {
        userAuditService.deleteUserAuditLogs(filter);
        return ResponseEntity.ok()
                .body(BaseResponse.success("Xóa log thành công."));
    }

    @DeleteMapping("/cron")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<Long>> manualCleanup(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok().body(BaseResponse.success(userAuditService.manualCleanup(startDate, endDate), "Dọn dẹp log thành công."));
    }
}
