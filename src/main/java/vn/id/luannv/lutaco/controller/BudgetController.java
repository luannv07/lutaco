package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.service.BudgetService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Budget API", description = "API quản lý ngân sách cá nhân")
@PreAuthorize("isAuthenticated()")
public class BudgetController {

    BudgetService budgetService;

    @Operation(
            summary = "Tạo budget mới",
            description = "Người dùng tạo budget mới (giới hạn theo user plan)"
    )
    @PostMapping
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Budget>> create(
            @Valid @RequestBody BudgetCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        budgetService.create(request),
                        MessageKeyConst.Success.CREATED
                ));
    }

    @Operation(
            summary = "Cập nhật budget",
            description = "Chỉnh sửa tên hoặc mô tả budget của chính mình"
    )
    @PutMapping("/{budgetName}")
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Budget>> update(
            @Parameter(description = "Tên budget cần cập nhật")
            @PathVariable String budgetName,
            @Valid @RequestBody BudgetUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        budgetService.update(budgetName, request),
                        MessageKeyConst.Success.UPDATED
                ));
    }

    @Operation(
            summary = "Xoá budget (user)",
            description = "Người dùng xoá budget của mình (chuyển sang INACTIVE)"
    )
    @DeleteMapping("/{budgetName}")
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(description = "Tên budget cần xoá")
            @PathVariable String budgetName
    ) {
        budgetService.deleteByUser(budgetName);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        null,
                        MessageKeyConst.Success.DELETED
                ));
    }

    @Operation(
            summary = "Lấy chi tiết budget",
            description = "Lấy thông tin chi tiết một budget của chính mình"
    )
    @GetMapping("/{budgetName}")
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Budget>> getDetail(
            @Parameter(description = "Tên budget cần lấy")
            @PathVariable String budgetName
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        budgetService.getDetail(budgetName),
                        MessageKeyConst.Success.SENT
                ));
    }

    @Operation(
            summary = "Lấy danh sách budget của tôi",
            description = "Lấy toàn bộ budget của user hiện tại"
    )
    @GetMapping
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<List<Budget>>> getMyBudgets() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        budgetService.getMyBudgets(),
                        MessageKeyConst.Success.SENT
                ));
    }

}
