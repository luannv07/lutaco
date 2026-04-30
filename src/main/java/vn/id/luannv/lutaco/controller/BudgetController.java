package vn.id.luannv.lutaco.controller;

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
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class BudgetController {

    BudgetService budgetService;

    @PostMapping
            public ResponseEntity<BaseResponse<BudgetResponse>> create(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        budgetService.create(request),
                        "Tạo ngân sách thành công."
                ));
    }

    @PatchMapping("/{id}/notification")
            public ResponseEntity<BaseResponse<Boolean>> preventDangerEmail(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        budgetService.preventDangerEmail(id),
                        "Cập nhật trạng thái cảnh báo cho Budget thành công."
                ));
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
            public ResponseEntity<BaseResponse<Page<BudgetResponse>>> search(@Valid @ModelAttribute BudgetFilterRequest request) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.search(request, request.getPage(), request.getSize()),
                        "Lấy danh sách ngân sách thành công."
                ));
    }

    @PutMapping("/{id}")
            public ResponseEntity<BaseResponse<BudgetResponse>> update(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        budgetService.update(id, request),
                        "Cập nhật ngân sách thành công."
                ));
    }

    @DeleteMapping("/{id}")
            public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        budgetService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success("Xóa ngân sách thành công.")
        );
    }
}
