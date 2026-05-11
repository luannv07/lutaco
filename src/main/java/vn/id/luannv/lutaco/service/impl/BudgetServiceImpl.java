package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetFilterRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.policy.PlanPolicy;
import vn.id.luannv.lutaco.repository.BudgetRepository;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.BudgetService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;
import vn.id.luannv.lutaco.util.StringUtils;
import vn.id.luannv.lutaco.util.TimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BudgetServiceImpl implements BudgetService {

    BudgetRepository budgetRepository;
    CategoryRepository categoryRepository;
    TransactionRepository transactionRepository;
    UserRepository userRepository;
    PlanPolicy planPolicy;

    @Override
    @Transactional
    public BudgetResponse create(BudgetCreateRequest request) {
        User user = userRepository.findByIdForUpdate(SecurityUtils.getCurrentId());

        int currentCount = budgetRepository.countBudgetByUser(user);
        if (!planPolicy.canCreateBudget(user, currentCount)) {
            log.warn("User {} has reached the maximum budget limit for their plan.", user.getUsername());
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED);
        }

        Category category = getCategoryOrThrow(request.getCategoryId());
        if (!category.isActiveFlg()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, Map.of("reason", "budget.category.inactive"));
        }

        Period period = EnumUtils.from(Period.class, request.getPeriod());

        if (budgetRepository.existsByUserIdAndCategoryIdAndPeriod(user.getId(), category.getId(), period)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, Map.of("field", "budget.user_category_period"));
        }

        LocalDate endDate = period.calculateEndDate(request.getStartDate());

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setName(request.getName());
        budget.setTargetAmount(request.getTargetAmount());
        budget.setPeriod(period);
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(endDate);

        recalculate(budget, user.getId(), category);

        budgetRepository.save(budget);
        log.info("Budget created for user: {}, category: {}, period: {}", user.getUsername(), category.getId(), period);

        return toResponse(budget);
    }

    @Override
    @Transactional
    public BudgetResponse update(Long id, BudgetCreateRequest request) {
        throw new UnsupportedOperationException("Use update(Long, BudgetUpdateRequest) instead.");
    }

    @Override
    @Transactional
    public BudgetResponse update(Long id, BudgetUpdateRequest request) {
        Budget budget = getMyBudgetOrThrow(id);
        Long userId = SecurityUtils.getCurrentId();

        if (StringUtils.hasText(request.getName())) {
            budget.setName(request.getName());
        }
        if (request.getTargetAmount() != null) {
            budget.setTargetAmount(request.getTargetAmount());
        }
        if (request.getStartDate() != null) {
            budget.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            budget.setEndDate(request.getEndDate());
        }

        validateDateRange(budget.getStartDate(), budget.getEndDate());
        recalculate(budget, userId, budget.getCategory());

        budgetRepository.save(budget);
        return toResponse(budget);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Budget budget = getMyBudgetOrThrow(id);
        budgetRepository.delete(budget);
        log.info("Budget {} deleted by user {}", id, SecurityUtils.getCurrentId());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getDetail(Long id) {
        return toResponse(getMyBudgetOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BudgetResponse> search(BudgetFilterRequest request) {
        Long userId = SecurityUtils.getCurrentId();
        Specification<Budget> spec = (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);

        if (StringUtils.hasText(request.getName())) {
            spec = spec.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("name")),
                    "%" + request.getName().trim().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(request.getPeriod())) {
            Period period = EnumUtils.tryFrom(Period.class, request.getPeriod()).orElse(null);
            if (period != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("period"), period));
            } else {
                log.warn("Invalid period filter value: {}", request.getPeriod());
            }
        }

        if (StringUtils.hasText(request.getStatus())) {
            try {
                BudgetStatus status = BudgetStatus.valueOf(request.getStatus().toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter value: {}", request.getStatus());
            }
        }

        if (request.getCategoryId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), request.getCategoryId()));
        }

        return budgetRepository.findAll(spec, request.pageable()).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getMyBudgets() {
        return budgetRepository.findByUserId(SecurityUtils.getCurrentId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BudgetResponse refreshProgress(Long id) {
        Budget budget = getMyBudgetOrThrow(id);
        recalculate(budget, SecurityUtils.getCurrentId(), budget.getCategory());
        budgetRepository.save(budget);
        return toResponse(budget);
    }

    @Override
    @Transactional
    public void refreshBudgetsForTransaction(Long userId, Long categoryId) {
        Category transactionCategory = getCategoryOrThrow(categoryId);
        Long parentCategoryId = transactionCategory.getParent() != null
                ? transactionCategory.getParent().getId()
                : null;

        List<Budget> budgets = budgetRepository.findByUserId(userId);
        List<Budget> affectedBudgets = budgets.stream()
                .filter(budget -> isAffectedByTransaction(budget, categoryId, parentCategoryId))
                .toList();

        if (affectedBudgets.isEmpty()) {
            log.debug("No matching budgets found for user {} and category {}", userId, categoryId);
            return;
        }

        affectedBudgets.forEach(budget -> recalculate(budget, userId, budget.getCategory()));
        budgetRepository.saveAll(affectedBudgets);
        log.info("Refreshed {} budget(s) for user {} and category {}", affectedBudgets.size(), userId, categoryId);
    }

    private void recalculate(Budget budget, Long userId, Category category) {
        List<Long> categoryIds = collectCategoryIds(category);

        Instant startInstant = TimeUtils.toUtcStartInstant(budget.getStartDate());
        Instant endInstant = budget.getEndDate() != null
                ? TimeUtils.toUtcExclusiveEndInstant(budget.getEndDate())
                : Instant.now().plusSeconds(1);

        Long sumLong = transactionRepository.sumAmountByCategoryIdsAndDateRange(userId, categoryIds, startInstant, endInstant);
        BigDecimal actual = BigDecimal.valueOf(sumLong != null ? sumLong : 0L);

        budget.setActualAmount(actual);
        budget.setPercentage(computePercentage(actual, budget.getTargetAmount()));
        budget.setStatus(determineStatus(budget.getPercentage()));
    }

    private List<Long> collectCategoryIds(Category category) {
        List<Long> ids = new ArrayList<>();
        ids.add(category.getId());
        category.getChildren().forEach(child -> ids.add(child.getId()));
        return ids;
    }

    private BigDecimal computePercentage(BigDecimal actual, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return actual.multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP);
    }

    private BudgetStatus determineStatus(BigDecimal percentage) {
        if (percentage == null) {
            return BudgetStatus.UNKNOWN;
        }
        int pct = percentage.intValue();
        if (pct <= BudgetStatus.NORMAL.getPercentage()) {
            return BudgetStatus.NORMAL;
        } else if (pct <= BudgetStatus.WARNING.getPercentage()) {
            return BudgetStatus.WARNING;
        } else {
            return BudgetStatus.DANGER;
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMS, Map.of("field", "startDate"));
        }
        if (endDate != null && !endDate.isAfter(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMS, Map.of("field", "endDate", "reason", "budget.date.end_before_start"));
        }
    }

    private Budget getMyBudgetOrThrow(Long id) {
        return budgetRepository.findByIdAndUserId(id, SecurityUtils.getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "category.id")));
    }

    private BudgetResponse toResponse(Budget budget) {
        if (budget == null) return null;

        BigDecimal actual = budget.getActualAmount() != null ? budget.getActualAmount() : BigDecimal.ZERO;
        BigDecimal target = budget.getTargetAmount() != null ? budget.getTargetAmount() : BigDecimal.ZERO;
        BigDecimal remaining = target.subtract(actual);

        return BudgetResponse.builder()
                .id(budget.getId())
                .name(budget.getName())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getCategoryCode() : null)
                .period(budget.getPeriod())
                .targetAmount(target)
                .actualAmount(actual)
                .remainingAmount(remaining)
                .percentage(budget.getPercentage())
                .status(budget.getStatus())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .userId(budget.getUser() != null ? budget.getUser().getId().toString() : null)
                .build();
    }

    private boolean isAffectedByTransaction(Budget budget, Long categoryId, Long parentCategoryId) {
        if (budget == null || budget.getCategory() == null) {
            return false;
        }

        Long budgetCategoryId = budget.getCategory().getId();
        return budgetCategoryId != null && (
                budgetCategoryId.equals(categoryId)
                        || budgetCategoryId.equals(parentCategoryId)
        );
    }
}
