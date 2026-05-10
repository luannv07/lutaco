package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetFilterRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.service.BudgetService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class BudgetController {

    BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BaseResponse<BudgetResponse>> create(
            @Valid @RequestBody BudgetCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        budgetService.create(request),
                        "Tạo ngân sách thành công."
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<BudgetResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetUpdateRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.update(id, request),
                        "Cập nhật ngân sách thành công."
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        budgetService.deleteById(id);
        return ResponseEntity.ok(BaseResponse.success("Xoá ngân sách thành công."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<BudgetResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.getDetail(id),
                        "Lấy chi tiết ngân sách thành công."
                ));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<BudgetResponse>>> getMyBudgets() {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.getMyBudgets(),
                        "Lấy danh sách ngân sách thành công."
                ));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Page<BudgetResponse>>> search(
            @Valid @ModelAttribute BudgetFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.search(request, request.getPage(), request.getSize()),
                        "Tìm kiếm ngân sách thành công."
                ));
    }

    @PostMapping("/{id}/refresh")
    public ResponseEntity<BaseResponse<BudgetResponse>> refreshProgress(@PathVariable Long id) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.refreshProgress(id),
                        "Cập nhật tiến độ ngân sách thành công."
                ));
    }
}
