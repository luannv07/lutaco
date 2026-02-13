package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.CreateBudgetRequest;
import vn.id.luannv.lutaco.dto.request.UpdateBudgetRequest;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;

import java.util.List;

public interface BudgetService {
    BudgetResponse createBudget(CreateBudgetRequest request);

    BudgetResponse updateBudget(Long id, UpdateBudgetRequest request);

    void deleteBudget(Long id);

    List<BudgetResponse> getBudgetsByCurrentUser();
}
