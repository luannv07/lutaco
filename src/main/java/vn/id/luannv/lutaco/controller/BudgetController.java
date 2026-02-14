package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.BudgetFilterRequest;
import vn.id.luannv.lutaco.dto.request.BudgetRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.service.BudgetService;

@Slf4j
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Budget API", description = "API quản lý ngân sách chi tiêu")
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class BudgetController {

    BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Tạo ngân sách mới")
    public ResponseEntity<BaseResponse<BudgetResponse>> create(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        budgetService.create(request),
                        "Tạo ngân sách thành công."
                ));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Tạo ngân sách mới")
    public ResponseEntity<BaseResponse<Boolean>> preventDangerEmail(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        budgetService.preventDangerEmail(id),
                        "Ko nhận email cảnh báo cho Budget thành công."
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết ngân sách")
    public ResponseEntity<BaseResponse<BudgetResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.getDetail(id),
                        "Lấy chi tiết ngân sách thành công."
                ));
    }

    @GetMapping
    @Operation(summary = "Tìm kiếm và phân trang ngân sách")
    public ResponseEntity<BaseResponse<Page<BudgetResponse>>> search(
            @ModelAttribute BudgetFilterRequest request,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.search(request, page, size),
                        "Lấy danh sách ngân sách thành công."
                ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật ngân sách")
    public ResponseEntity<BaseResponse<BudgetResponse>> update(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.update(id, request),
                        "Cập nhật ngân sách thành công."
                ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoá ngân sách")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        budgetService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success("Xóa ngân sách thành công.")
        );
    }
}
