package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.BudgetFilterRequest;
import vn.id.luannv.lutaco.dto.request.BudgetRequest;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;
import vn.id.luannv.lutaco.service.BudgetService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BudgetServiceImpl implements BudgetService {

    @Override
    public BudgetResponse create(BudgetRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public Boolean preventDangerEmail(Long id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private LocalDate calculateEndDate(LocalDate startDate, Period period) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getDetail(Long id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Page<BudgetResponse> search(BudgetFilterRequest request, Integer page, Integer size) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public BudgetResponse update(Long id, BudgetRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private BudgetStatus updateStatus(float percentage) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private BudgetResponse convertToResponse(Budget budget) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
