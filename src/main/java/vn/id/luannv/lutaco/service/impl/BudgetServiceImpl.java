package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.BudgetMapper;
import vn.id.luannv.lutaco.repository.BudgetRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.BudgetService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BudgetServiceImpl implements BudgetService {

    BudgetRepository budgetRepository;
    BudgetMapper budgetMapper;
    UserRepository userRepository;

    @Override
    public Budget create(BudgetCreateRequest request) {

        String userId = SecurityUtils.getCurrentId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        long count = budgetRepository.countByUser_Id(userId);
        if (count >= user.getUserPlan().getMaxBudgets()) {
            throw new BusinessException(ErrorCode.BUDGET_LIMIT_EXCEEDED);
        }

        Budget budget = budgetMapper.toEntity(request);
        budget.setUser(user);
        budget.setStatus(
                request.getStatus() != null ? request.getStatus() : BudgetStatus.ACTIVE
        );

        return budgetRepository.save(budget);
    }

    @Override
    public Budget update(String budgetName, BudgetUpdateRequest request) {

        Budget budget = getMyBudgetOrThrow(budgetName);
        budgetMapper.update(budget, request);
        return budgetRepository.save(budget);
    }

    /**
     * User xoá → INACTIVE (có thể khôi phục)
     */
    @Override
    public void deleteByUser(String budgetName) {

        Budget budget = getMyBudgetOrThrow(budgetName);
        budget.setStatus(BudgetStatus.INACTIVE);
        budgetRepository.save(budget);
    }

    /**
     * Admin xoá → ARCHIVED (vĩnh viễn)
     */
    @Override
    public void archiveByAdmin(String userId, String budgetName) {

        Budget budget = budgetRepository
                .findByUser_IdAndBudgetName(userId, budgetName)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENUM_NOT_FOUND));

        budget.setStatus(BudgetStatus.ARCHIVED);
        budgetRepository.save(budget);
    }

    @Override
    public Budget getDetail(String budgetName) {
        return getMyBudgetOrThrow(budgetName);
    }

    @Override
    public List<Budget> getMyBudgets() {
        return budgetRepository.findByUser_Id(
                SecurityUtils.getCurrentId()
        );
    }

    private Budget getMyBudgetOrThrow(String budgetName) {
        return budgetRepository
                .findByUser_IdAndBudgetName(
                        SecurityUtils.getCurrentId(), budgetName
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }
}

