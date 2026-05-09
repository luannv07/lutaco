package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetFilterRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;

import java.util.List;

public interface BudgetService extends BaseService<BudgetFilterRequest, BudgetResponse, BudgetCreateRequest, Long> {

    BudgetResponse update(Long id, BudgetUpdateRequest request);

    List<BudgetResponse> getMyBudgets();

    BudgetResponse refreshProgress(Long id);
}
