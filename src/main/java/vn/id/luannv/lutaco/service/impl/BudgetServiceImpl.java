package vn.id.luannv.lutaco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.BudgetDTO;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.BudgetMapper;
import vn.id.luannv.lutaco.repository.BudgetRepository;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.BudgetService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    @Override
    public BudgetDTO createBudget(BudgetDTO budgetDTO) {
        String currentUserId = SecurityUtils.getCurrentId();
        long currentBudgetCount = budgetRepository.countByUserIdAndDeletedAtIsNull(currentUserId);

        if (currentBudgetCount >= SecurityUtils.getCurrentUserPlan().getMaxBudgetCount()) {
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED);
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Category category = categoryRepository.findById(budgetDTO.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Budget budget = budgetMapper.toEntity(budgetDTO);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setActualAmount(0L);
        budget.setPercentage(0.0f);
        budget.setStatus(BudgetStatus.IN_PROGRESS);

        if (budgetDTO.getStartDate() == null) {
            budget.setStartDate(LocalDateTime.now());
        }
        calculateEndDate(budget);

        Budget savedBudget = budgetRepository.save(budget);
        return budgetMapper.toDTO(savedBudget);
    }

    @Override
    public BudgetDTO updateBudget(Long id, BudgetDTO budgetDTO) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (budget.getStatus() == BudgetStatus.DONE) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        budgetMapper.updateFromDTO(budgetDTO, budget);

        calculateEndDate(budget);
        updatePercentage(budget);

        Budget updatedBudget = budgetRepository.save(budget);
        return budgetMapper.toDTO(updatedBudget);
    }

    @Override
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        budget.setDeletedAt(LocalDateTime.now());
        budgetRepository.save(budget);
    }

    @Override
    public List<BudgetDTO> getBudgetsByCurrentUser() {
        String currentUserId = SecurityUtils.getCurrentId();
        List<Budget> budgets = budgetRepository.findByUserIdAndDeletedAtIsNull(currentUserId);
        return budgets.stream()
                .map(budget -> {
                    updatePercentage(budget);
                    return budgetMapper.toDTO(budget);
                })
                .collect(Collectors.toList());
    }

    private void calculateEndDate(Budget budget) {
        LocalDateTime startDate = budget.getStartDate();
        Period period = budget.getPeriod();
        LocalDateTime endDate;

        switch (period) {
            case DAY:
                endDate = startDate.plusDays(1);
                break;
            case WEEK:
                endDate = startDate.plusWeeks(1);
                break;
            case MONTH:
                endDate = startDate.plusMonths(1);
                break;
            case YEAR:
                endDate = startDate.plusYears(1);
                break;
            default:
                throw new BusinessException(ErrorCode.INVALID_PARAMS);
        }
        budget.setEndDate(endDate);
    }

    private void updatePercentage(Budget budget) {
        if (budget.getTargetAmount() > 0) {
            float percentage = ((float) budget.getActualAmount() / budget.getTargetAmount()) * 100;
            budget.setPercentage(percentage);
            if (percentage >= 100) {
                budget.setStatus(BudgetStatus.DONE);
            }
        }
    }
}
