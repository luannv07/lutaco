package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.BudgetDTO;

import java.util.List;

public interface BudgetService {
    BudgetDTO createBudget(BudgetDTO budgetDTO);

    BudgetDTO updateBudget(Long id, BudgetDTO budgetDTO);

    void deleteBudget(Long id);

    List<BudgetDTO> getBudgetsByCurrentUser();
}
