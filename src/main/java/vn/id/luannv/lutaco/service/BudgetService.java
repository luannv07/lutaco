package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.entity.Budget;

import java.util.List;

public interface BudgetService {

    Budget create(BudgetCreateRequest request);

    Budget update(String budgetName, BudgetUpdateRequest request);

    void deleteByUser(String budgetName);

    void archiveByAdmin(String userId, String budgetName);

    Budget getDetail(String budgetName);

    List<Budget> getMyBudgets();
}
