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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.id.luannv.lutaco.dto.EnumDisplay;
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
import vn.id.luannv.lutaco.util.LocalizationUtils;
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
    LocalizationUtils localizationUtils;

    @Override
    public BudgetResponse create(BudgetRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        String userId = SecurityUtils.getCurrentId();
        log.info("[{}]: Attempting to create budget for user ID: {}", username, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[{}]: User with ID {} not found during budget creation.", username, userId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("username", userId));
                });

        if (Objects.isNull(user.getUserPlan())) {
            log.warn("[{}]: User ID {} has no user plan configured, cannot create budget.", username, userId);
            throw new BusinessException(ErrorCode.PLAN_NOT_CONFIGURED);
        }

        long countByUser = budgetRepository.countByUser(user);
        if (countByUser >= user.getUserPlan().getMaxBudgetsCount()) {
            log.warn("[{}]: User ID {} has reached maximum budget limit ({}). Cannot create new budget.", username, userId, user.getUserPlan().getMaxBudgetsCount());
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED, Map.of("limitCount", user.getUserPlan().getMaxBudgetsCount()));
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("[{}]: Category with ID {} not found for budget creation.", username, request.getCategoryId());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                });

        if (budgetRepository.existsByUserAndCategory(user, category)) {
            log.warn("[{}]: Budget already exists for user ID {} and category ID {}.", username, userId, request.getCategoryId());
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

        if (request.getPeriod() != null) {
            Period period = EnumUtils.from(Period.class, request.getPeriod());
            budget.setPeriod(period);
            budget.setEndDate(calculateEndDate(request.getStartDate(), period));
        }

        Budget savedBudget = budgetRepository.save(budget);
        log.info("[{}]: Budget with ID {} created successfully for user ID {} and category ID {}.", username, savedBudget.getId(), userId, category.getId());
        return convertToResponse(savedBudget);
    }

    @Override
    @Transactional
    public Boolean preventDangerEmail(Long id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to prevent danger email for budget ID: {}", username, id);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Budget with ID {} not found or not owned by user.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });
        SecurityUtils.assertOwnerOrAdmin(budget.getUser().getId());

        // toggle status
        if (budget.getStatus() == BudgetStatus.UNKNOWN) {
            budget.setStatus(BudgetStatus.NORMAL);
        } else {
            budget.setStatus(BudgetStatus.UNKNOWN);
        }

        budgetRepository.save(budget);

        log.info("[{}]: Budget ID {} status toggled.", username, id);

        return true;
    }

    private LocalDate calculateEndDate(LocalDate startDate, Period period) {
        if (startDate == null || period == null) {
            log.debug("[system]: Start date or period is null, cannot calculate end date.");
            return null;
        }
        LocalDate endDate = switch (period) {
            case DAY -> startDate;
            case WEEK -> startDate.plusWeeks(1).minusDays(1);
            case MONTH -> startDate.plusMonths(1).minusDays(1);
            case YEAR -> startDate.plusYears(1).minusDays(1);
        };
        log.debug("[system]: Calculated end date for start date {} and period {}: {}", startDate, period, endDate);
        return endDate;
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getDetail(Long id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Fetching details for budget ID: {}", username, id);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Budget with ID {} not found.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });

        SecurityUtils.assertOwnerOrAdmin(budget.getUser().getId());
        log.info("[{}]: Successfully retrieved details for budget ID {}.", username, id);
        return convertToResponse(budget);
    }

    @Override
    public Page<BudgetResponse> search(BudgetFilterRequest request, Integer page, Integer size) {
        String username = SecurityUtils.getCurrentUsername();
        Specification<Budget> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getName())) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + request.getName() + "%"));
            }

            if (StringUtils.hasText(request.getPeriod())) {
                Period period = EnumUtils.from(Period.class, request.getPeriod());
                predicates.add(criteriaBuilder.equal(root.get("period"), period));
            }

            if (!SecurityUtils.isAdmin()) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), SecurityUtils.getCurrentId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<BudgetResponse> result = budgetRepository.findAll(spec, PageRequest.of(page - 1, size))
                .map(this::convertToResponse);
        log.info("[{}]: Found {} budgets matching the search criteria.", username, result.getTotalElements());
        return result;
    }

    @Override
    @Transactional
    public BudgetResponse update(Long id, BudgetRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Updating budget with ID: {} with request: {}", username, id, request);
        Budget existingBudget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Budget with ID {} not found for update.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });
        SecurityUtils.assertOwnerOrAdmin(existingBudget.getUser().getId());

        if (existingBudget.getPercentage() != 0 || existingBudget.getActualAmount() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        budgetMapper.update(existingBudget, request);

        Period period = existingBudget.getPeriod();
        if (request.getPeriod() != null) {
            period = EnumUtils.from(Period.class, request.getPeriod());
            existingBudget.setPeriod(period);
            log.debug("[{}]: Budget ID {} period updated to {}.", username, id, period);
        }

        LocalDate startDate = existingBudget.getStartDate();
        if (request.getStartDate() != null) {
            startDate = request.getStartDate();
            existingBudget.setStartDate(startDate);
            log.debug("[{}]: Budget ID {} start date updated to {}.", username, id, startDate);
        }

        existingBudget.setEndDate(calculateEndDate(startDate, period));
        float percentage = CustomizeNumberUtils.percentage(existingBudget.getActualAmount(), existingBudget.getTargetAmount());
        existingBudget.setPercentage(percentage);
        existingBudget.setStatus(updateStatus(percentage));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        existingBudget.setCategory(category);

        Budget updatedBudget = budgetRepository.save(existingBudget);
        log.info("[{}]: Budget with ID {} updated successfully.", username, updatedBudget.getId());
        return convertToResponse(updatedBudget);
    }

    @Override
    public void deleteById(Long id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to delete budget with ID: {}", username, id);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Budget with ID {} not found for deletion.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("budgetId", id));
                });

        SecurityUtils.assertOwnerOrAdmin(budget.getUser().getId());
        budgetRepository.delete(budget);
        log.info("[{}]: Budget with ID {} deleted successfully.", username, id);
    }

    private BudgetStatus updateStatus(float percentage) {
        String username = SecurityUtils.getCurrentUsername();
        if (percentage > BudgetStatus.DANGER.getPercentage()) {
            log.debug("[{}]: Budget status set to DANGER (percentage: {}).", username, percentage);
            return BudgetStatus.DANGER;
        }
        if (percentage > BudgetStatus.WARNING.getPercentage()) {
            log.debug("[{}]: Budget status set to WARNING (percentage: {}).", username, percentage);
            return BudgetStatus.WARNING;
        }
        if (percentage > BudgetStatus.UNKNOWN.getPercentage()) {
            log.debug("[{}]: Budget status set to NORMAL (percentage: {}).", username, percentage);
            return BudgetStatus.NORMAL;
        }
        log.debug("[{}]: Budget status set to UNKNOWN (percentage: {}).", username, percentage);
        return BudgetStatus.UNKNOWN;
    }

    private BudgetResponse convertToResponse(Budget budget) {
        BudgetResponse response = budgetMapper.toDto(budget);
        response.setStatus(new EnumDisplay<>(budget.getStatus(), localizationUtils.getLocalizedMessage(budget.getStatus().getDisplay())));
        response.setPeriod(new EnumDisplay<>(budget.getPeriod(), localizationUtils.getLocalizedMessage(budget.getPeriod().getDisplay())));
        return response;
    }
}
