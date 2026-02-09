package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.entity.RecurringTransaction;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.RecurringTransactionMapper;
import vn.id.luannv.lutaco.repository.RecurringTransactionRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.service.RecurringTransactionService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    RecurringTransactionRepository recurringTransactionRepository;
    RecurringTransactionMapper recurringTransactionMapper;
    TransactionRepository transactionRepository;

    @Override
    public RecurringTransactionResponse create(RecurringTransactionRequest request) {
        log.info("RecurringTransactionServiceImpl create: {}", request);

        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        RecurringTransaction recurringTransaction = recurringTransactionMapper.toEntity(request);
        recurringTransaction.setTransaction(transaction);
        FrequentType frequentType = FrequentType.from(request.getFrequentType());
        recurringTransaction.setFrequentType(frequentType);
        recurringTransaction.setNextDate(calculateNextDate(request.getStartDate(), frequentType));

        return recurringTransactionMapper.toResponse(
                recurringTransactionRepository.save(recurringTransaction)
        );
    }

    @Override
    public RecurringTransactionResponse getDetail(Long id) {
        log.info("RecurringTransactionServiceImpl getDetail: {}", id);

        return recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .map(recurringTransactionMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Override
    public Page<RecurringTransactionResponse> search(RecurringTransactionFilterRequest request, Integer page, Integer size) {
        log.info("RecurringTransactionServiceImpl search: {}", request);

        Pageable pageable = PageRequest.of(page - 1, size);
        return recurringTransactionRepository.findByFilters(request, SecurityUtils.getCurrentId(), pageable)
                .map(recurringTransactionMapper::toResponse);
    }

    @Override
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) {
        log.info("RecurringTransactionServiceImpl update: {}, {}", id, request);

        RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        recurringTransactionMapper.updateEntity(recurringTransaction, request);
        FrequentType frequentType = FrequentType.from(request.getFrequentType());
        recurringTransaction.setFrequentType(frequentType);
        recurringTransaction.setNextDate(calculateNextDate(recurringTransaction.getStartDate(), frequentType));


        return recurringTransactionMapper.toResponse(
                recurringTransactionRepository.save(recurringTransaction)
        );
    }

    @Override
    public void deleteById(Long id) {
        log.info("RecurringTransactionServiceImpl deleteById: {}", id);
        RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        recurringTransactionRepository.deleteById(recurringTransaction.getId());
    }

    private LocalDate calculateNextDate(LocalDate startDate, FrequentType frequentType) {
        if (startDate == null || frequentType == null) {
            return null;
        }
        return switch (frequentType) {
            case DAILY -> startDate.plusDays(1);
            case WEEKLY -> startDate.plusWeeks(1);
            case MONTHLY -> startDate.plusMonths(1);
            case YEARLY -> startDate.plusYears(1);
        };
    }
}
