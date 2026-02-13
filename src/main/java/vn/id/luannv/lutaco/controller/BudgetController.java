package vn.id.luannv.lutaco.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.BudgetDTO;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.service.BudgetService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BaseResponse<BudgetDTO>> createBudget(@RequestBody BudgetDTO budgetDTO) {
        BudgetDTO createdBudget = budgetService.createBudget(budgetDTO);
        return ResponseEntity.ok(BaseResponse.success(createdBudget, "Tạo ngân sách thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<BudgetDTO>> updateBudget(@PathVariable Long id, @RequestBody BudgetDTO budgetDTO) {
        BudgetDTO updatedBudget = budgetService.updateBudget(id, budgetDTO);
        return ResponseEntity.ok(BaseResponse.success(updatedBudget, "Cập nhật ngân sách thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(BaseResponse.success("Xóa ngân sách thành công"));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<BudgetDTO>>> getBudgetsByCurrentUser() {
        List<BudgetDTO> budgets = budgetService.getBudgetsByCurrentUser();
        return ResponseEntity.ok(BaseResponse.success(budgets, "Lấy danh sách ngân sách thành công"));
    }
}
