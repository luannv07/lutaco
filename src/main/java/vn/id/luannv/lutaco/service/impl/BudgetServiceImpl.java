package vn.id.luannv.lutaco.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    BudgetRepository budgetRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    BudgetMapper budgetMapper;

    @Override
    public BudgetResponse create(BudgetRequest request) {
        User user = userRepository.getReferenceById(SecurityUtils.getCurrentId());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Budget budget = budgetMapper.toEntity(request);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setActualAmount(0L); // Initialize actual amount
        budget.setPeriod(EnumUtils.from(Period.class, request.getPeriod()));

        Budget savedBudget = budgetRepository.save(budget);
        log.info("Budget with id {} created successfully", savedBudget.getId());
        return budgetMapper.toDto(savedBudget);
    }

    @Override
    public BudgetResponse getDetail(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
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
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        budgetMapper.update(existingBudget, request);

        if (request.getPeriod() != null) {
            existingBudget.setPeriod(EnumUtils.from(Period.class, request.getPeriod()));
        }
        if (request.getStartDate() != null) {
            existingBudget.setStartDate(request.getStartDate());
        }

        Budget updatedBudget = budgetRepository.save(existingBudget);
        log.info("Budget with id {} updated successfully", updatedBudget.getId());
        return budgetMapper.toDto(updatedBudget);
    }

    @Override
    public void deleteById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        budgetRepository.delete(budget);
        log.info("Budget with id {} deleted successfully", id);
    }
}
