package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.CreateBudgetRequest;
import vn.id.luannv.lutaco.dto.request.UpdateBudgetRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.service.BudgetService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BaseResponse<BudgetResponse>> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse createdBudget = budgetService.createBudget(request);
        return ResponseEntity.ok(BaseResponse.success(createdBudget, "Tạo ngân sách thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<BudgetResponse>> updateBudget(@PathVariable Long id, @Valid @RequestBody UpdateBudgetRequest request) {
        BudgetResponse updatedBudget = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(BaseResponse.success(updatedBudget, "Cập nhật ngân sách thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(BaseResponse.success("Xóa ngân sách thành công"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<BudgetResponse>>> getBudgetsByCurrentUser() {
        List<BudgetResponse> budgets = budgetService.getBudgetsByCurrentUser();
        return ResponseEntity.ok(BaseResponse.success(budgets, "Lấy danh sách ngân sách thành công"));
    }
}
