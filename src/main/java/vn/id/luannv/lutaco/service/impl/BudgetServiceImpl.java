package vn.id.luannv.lutaco.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.id.luannv.lutaco.dto.request.BudgetFilterRequest;
import vn.id.luannv.lutaco.dto.request.BudgetRequest;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.Period;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.BudgetMapper;
import vn.id.luannv.lutaco.repository.BudgetRepository;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.BudgetService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BudgetServiceImpl implements BudgetService {

    BudgetRepository budgetRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    BudgetMapper budgetMapper;

    @Override
    public BudgetResponse create(BudgetRequest request) {
        String userId = SecurityUtils.getCurrentId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("username", userId)));

        if (Objects.isNull(user.getUserPlan())) {
            throw new BusinessException(ErrorCode.PLAN_NOT_CONFIGURED);
        }

        long countByUser = budgetRepository.countByUser(user);
        if (countByUser >= user.getUserPlan().getMaxBudgetsCount()) {
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED, Map.of("limitCount", user.getUserPlan().getMaxBudgetsCount()));
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId())));

        if (budgetRepository.existsByUserAndCategory(user, category))
            throw new BusinessException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    Map.of("categoryId", request.getCategoryId())
            );

        Budget budget = budgetMapper.toEntity(request);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setActualAmount(0L); // Initialize actual amount
        budget.setPercentage(0F);

        Period period = EnumUtils.from(Period.class, request.getPeriod());
        budget.setPeriod(period);
        budget.setEndDate(calculateEndDate(request.getStartDate(), period));

        Budget savedBudget = budgetRepository.save(budget);
        log.info("Budget with id {} created successfully", savedBudget.getId());
        return budgetMapper.toDto(savedBudget);
    }

    private LocalDate calculateEndDate(LocalDate startDate, Period period) {
        if (startDate == null || period == null) {
            return null;
        }
        return switch (period) {
            case DAY -> startDate;
            case WEEK -> startDate.plusWeeks(1).minusDays(1);
            case MONTH -> startDate.plusMonths(1).minusDays(1);
            case YEAR -> startDate.plusYears(1).minusDays(1);
        };
    }

    @Override
    public BudgetResponse getDetail(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id)));
        return budgetMapper.toDto(budget);
    }

    @Override
    public Page<BudgetResponse> search(BudgetFilterRequest request, Integer page, Integer size) {
        Specification<Budget> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getName())) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + request.getName() + "%"));
            }

            if (StringUtils.hasText(request.getPeriod())) {
                Period period = EnumUtils.from(Period.class, request.getPeriod());
                predicates.add(criteriaBuilder.equal(root.get("period"), period));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return budgetRepository.findAll(spec, PageRequest.of(page, size))
                .map(budgetMapper::toDto);
    }

    @Override
    public BudgetResponse update(Long id, BudgetRequest request) {
        Budget existingBudget = budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id)));

        budgetMapper.update(existingBudget, request);

        Period period = existingBudget.getPeriod();
        if (request.getPeriod() != null) {
            period = EnumUtils.from(Period.class, request.getPeriod());
            existingBudget.setPeriod(period);
        }

        LocalDate startDate = existingBudget.getStartDate();
        if (request.getStartDate() != null) {
            startDate = request.getStartDate();
            existingBudget.setStartDate(startDate);
        }

        // Recalculate endDate if period or startDate changes
        existingBudget.setEndDate(calculateEndDate(startDate, period));


        Budget updatedBudget = budgetRepository.save(existingBudget);
        log.info("Budget with id {} updated successfully", updatedBudget.getId());
        return budgetMapper.toDto(updatedBudget);
    }

    @Override
    public void deleteById(Long id) {
        log.info("delete budget with id {}", id);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id)));
        budgetRepository.delete(budget);
        log.info("Budget with id {} deleted successfully", id);
    }
}
