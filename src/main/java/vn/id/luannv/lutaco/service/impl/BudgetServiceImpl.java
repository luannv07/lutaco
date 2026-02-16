package vn.id.luannv.lutaco.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.BudgetMapper;
import vn.id.luannv.lutaco.repository.BudgetRepository;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.BudgetService;
import vn.id.luannv.lutaco.util.CustomizeNumberUtils;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @CacheEvict(value = "budgets", key = "@securityPermission.getCurrentUserId()")
    public BudgetResponse create(BudgetRequest request) {
        String userId = SecurityUtils.getCurrentId();
        log.info("Attempting to create budget for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found during budget creation.", userId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("username", userId));
                });

        if (Objects.isNull(user.getUserPlan())) {
            log.warn("User ID {} has no user plan configured, cannot create budget.", userId);
            throw new BusinessException(ErrorCode.PLAN_NOT_CONFIGURED);
        }

        long countByUser = budgetRepository.countByUser(user);
        if (countByUser >= user.getUserPlan().getMaxBudgetsCount()) {
            log.warn("User ID {} has reached maximum budget limit ({}). Cannot create new budget.", userId, user.getUserPlan().getMaxBudgetsCount());
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED, Map.of("limitCount", user.getUserPlan().getMaxBudgetsCount()));
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category with ID {} not found for budget creation.", request.getCategoryId());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                });

        if (budgetRepository.existsByUserAndCategory(user, category)) {
            log.warn("Budget already exists for user ID {} and category ID {}.", userId, request.getCategoryId());
            throw new BusinessException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    Map.of("categoryId", request.getCategoryId())
            );
        }

        Budget budget = budgetMapper.toEntity(request);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setActualAmount(0L); // Initialize actual amount
        budget.setPercentage(0F);
        budget.setStatus(BudgetStatus.NORMAL);

        Period period = EnumUtils.from(Period.class, request.getPeriod());
        budget.setPeriod(period);
        budget.setEndDate(calculateEndDate(request.getStartDate(), period));

        Budget savedBudget = budgetRepository.save(budget);
        log.info("Budget with ID {} created successfully for user ID {} and category ID {}.", savedBudget.getId(), userId, category.getId());
        return budgetMapper.toDto(savedBudget);
    }

    @Override
    @CacheEvict(value = "budgets", key = "#id")
    public Boolean preventDangerEmail(Long id) {
        log.info("Attempting to prevent danger email for budget ID: {}", id);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Budget with ID {} not found for preventing danger email.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });
        budget.setStatus(BudgetStatus.UNKNOWN);
        budgetRepository.save(budget);
        log.info("Budget ID {} status updated to UNKNOWN to prevent danger emails.", id);
        return true;
    }

    private LocalDate calculateEndDate(LocalDate startDate, Period period) {
        if (startDate == null || period == null) {
            log.debug("Start date or period is null, cannot calculate end date.");
            return null;
        }
        LocalDate endDate = switch (period) {
            case DAY -> startDate;
            case WEEK -> startDate.plusWeeks(1).minusDays(1);
            case MONTH -> startDate.plusMonths(1).minusDays(1);
            case YEAR -> startDate.plusYears(1).minusDays(1);
        };
        log.debug("Calculated end date for start date {} and period {}: {}", startDate, period, endDate);
        return endDate;
    }

    @Override
    @Cacheable(value = "budgets", key = "#id")
    public BudgetResponse getDetail(Long id) {
        log.info("Fetching details for budget ID: {}", id);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Budget with ID {} not found for detail retrieval.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });
        log.info("Successfully retrieved details for budget ID {}.", id);
        return budgetMapper.toDto(budget);
    }

    @Override
    @Cacheable(value = "budgetsList", key = "{#request, #page, #size}")
    public Page<BudgetResponse> search(BudgetFilterRequest request, Integer page, Integer size) {
        log.info("Searching budgets with filter: {}, page: {}, size: {}.", request, page, size);
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

        Page<BudgetResponse> result = budgetRepository.findAll(spec, PageRequest.of(page, size))
                .map(budgetMapper::toDto);
        log.info("Found {} budgets matching the search criteria.", result.getTotalElements());
        return result;
    }

    @Override
    @CacheEvict(value = "budgets", key = "#id")
    public BudgetResponse update(Long id, BudgetRequest request) {
        log.info("Updating budget with ID: {} with request: {}", id, request);
        Budget existingBudget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Budget with ID {} not found for update.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });

        budgetMapper.update(existingBudget, request);

        Period period = existingBudget.getPeriod();
        if (request.getPeriod() != null) {
            period = EnumUtils.from(Period.class, request.getPeriod());
            existingBudget.setPeriod(period);
            log.debug("Budget ID {} period updated to {}.", id, period);
        }

        LocalDate startDate = existingBudget.getStartDate();
        if (request.getStartDate() != null) {
            startDate = request.getStartDate();
            existingBudget.setStartDate(startDate);
            log.debug("Budget ID {} start date updated to {}.", id, startDate);
        }

        // Recalculate endDate if period or startDate changes
        existingBudget.setEndDate(calculateEndDate(startDate, period));
        float percentage = CustomizeNumberUtils.percentage(existingBudget.getActualAmount(), existingBudget.getTargetAmount());
        existingBudget.setPercentage(percentage);
        existingBudget.setStatus(updateStatus(percentage));

        Budget updatedBudget = budgetRepository.save(existingBudget);
        log.info("Budget with ID {} updated successfully.", updatedBudget.getId());
        return budgetMapper.toDto(updatedBudget);
    }

    @Override
    @CacheEvict(value = "budgets", key = "#id")
    public void deleteById(Long id) {
        log.info("Attempting to delete budget with ID: {}", id);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Budget with ID {} not found for deletion.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });
        budgetRepository.delete(budget);
        log.info("Budget with ID {} deleted successfully.", id);
    }

    private BudgetStatus updateStatus(float percentage) {
        if (percentage > BudgetStatus.DANGER.getPercentage()) {
            log.debug("Budget status set to DANGER (percentage: {}).", percentage);
            return BudgetStatus.DANGER;
        }
        if (percentage > BudgetStatus.WARNING.getPercentage()) {
            log.debug("Budget status set to WARNING (percentage: {}).", percentage);
            return BudgetStatus.WARNING;
        }
        if (percentage > BudgetStatus.UNKNOWN.getPercentage()) {
            log.debug("Budget status set to NORMAL (percentage: {}).", percentage);
            return BudgetStatus.NORMAL;
        }
        log.debug("Budget status set to UNKNOWN (percentage: {}).", percentage);
        return BudgetStatus.UNKNOWN;
    }
}
