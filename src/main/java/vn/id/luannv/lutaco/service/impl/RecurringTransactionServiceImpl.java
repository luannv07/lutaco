package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.RecurringTransactionMapper;
import vn.id.luannv.lutaco.repository.RecurringTransactionRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.service.RecurringTransactionService;
import vn.id.luannv.lutaco.util.EnumUtils;
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
    ApplicationEventPublisher applicationEventPublisher;

    private record InternalState(
            RecurringTransaction recurringTransaction,
            RecurringTransactionEvent.RecurringUserFields recurringUserFields
    ) {}

    @Override
    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request) {
        log.info("Attempting to create a new recurring transaction with request: {}", request);
        InternalState state = createAndSaveTransaction(request);
        publishEvent(state, RecurringTransactionEvent.RecurringTransactionState.INITIALIZER);
        log.info("Successfully created recurring transaction with ID: {}", state.recurringTransaction().getId());
        return recurringTransactionMapper.toResponse(state.recurringTransaction());
    }

    @Transactional
    public void createWithCronJob(RecurringTransactionRequest request) {
        log.info("Creating recurring transaction via cron job with request: {}", request);
        InternalState state = createAndSaveTransaction(request);
        publishEvent(state, RecurringTransactionEvent.RecurringTransactionState.FREQUENCY);
        log.info("Recurring transaction created via cron job for transaction ID: {}", request.getTransactionId());
    }

    @Override
    @Transactional
    public void processOne(RecurringTransaction rt) {
        log.info("Processing single recurring transaction with ID: {}", rt.getId());
        RecurringTransactionEvent.RecurringUserFields recurringUserFields = transactionRepository
                .getRecurringUserFieldsByTransactionId(rt.getTransaction().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        InternalState state = new InternalState(rt, recurringUserFields);
        publishEvent(state, RecurringTransactionEvent.RecurringTransactionState.FREQUENCY);

        rt.setNextDate(rt.getFrequentType().calculateNextDate(LocalDate.now()));
        recurringTransactionRepository.save(rt);
        log.info("Finished processing recurring transaction with ID: {}. Next scheduled date: {}", rt.getId(), rt.getNextDate());
    }

    private InternalState createAndSaveTransaction(RecurringTransactionRequest request) {
        RecurringTransactionEvent.RecurringUserFields recurringUserFields = transactionRepository
                .getRecurringUserFieldsByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Transaction transaction = transactionRepository.getReferenceById(request.getTransactionId());
        RecurringTransaction recurringTransaction = recurringTransactionMapper.toEntity(request);
        recurringTransaction.setTransaction(transaction);
        FrequentType frequentType = EnumUtils.from(FrequentType.class, request.getFrequentType());
        recurringTransaction.setFrequentType(frequentType);
        recurringTransaction.setNextDate(frequentType.calculateNextDate(request.getStartDate()));

        RecurringTransaction savedTransaction = recurringTransactionRepository.save(recurringTransaction);
        return new InternalState(savedTransaction, recurringUserFields);
    }

    private void publishEvent(InternalState state, RecurringTransactionEvent.RecurringTransactionState eventState) {
        RecurringTransaction recurringTransaction = state.recurringTransaction();
        FrequentType frequentType = recurringTransaction.getFrequentType();
        LocalDate nextPaymentDate = frequentType.calculateNextDate(recurringTransaction.getStartDate());

        Object event = switch (eventState) {
            case INITIALIZER -> RecurringTransactionEvent.RecurringInitialization.builder()
                    .recurringTransactionId(recurringTransaction.getId())
                    .nextPaymentDate(nextPaymentDate)
                    .frequentType(frequentType)
                    .recurringUserFields(state.recurringUserFields())
                    .startDate(recurringTransaction.getStartDate())
                    .createdDate(recurringTransaction.getCreatedDate())
                    .build();
            case FREQUENCY -> RecurringTransactionEvent.RecurringFrequency.builder()
                    .recurringTransactionId(recurringTransaction.getId())
                    .nextPaymentDate(nextPaymentDate)
                    .frequentType(frequentType)
                    .recurringUserFields(state.recurringUserFields())
                    .build();
        };
        applicationEventPublisher.publishEvent(event);
        log.debug("Published event {} for recurring transaction ID: {}", eventState, recurringTransaction.getId());
    }

    @Override
    public RecurringTransactionResponse getDetail(Long id) {
        log.info("Fetching recurring transaction details for ID: {}", id);

        return recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .map(recurringTransactionMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Recurring transaction with ID: {} not found for current user.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });
    }

    @Override
    public Page<RecurringTransactionResponse> search(RecurringTransactionFilterRequest request, Integer page, Integer size) {
        log.info("Searching recurring transactions for current user with filter: {}", request);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<RecurringTransactionResponse> result = recurringTransactionRepository.findByFilters(request, SecurityUtils.getCurrentId(), pageable)
                .map(recurringTransactionMapper::toResponse);
        log.info("Found {} recurring transactions matching the criteria.", result.getTotalElements());
        return result;
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) {
        log.info("Updating recurring transaction with ID: {} using request: {}", id, request);

        RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .orElseThrow(() -> {
                    log.warn("Recurring transaction with ID: {} not found for current user for update.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        recurringTransactionMapper.updateEntity(recurringTransaction, request);
        FrequentType frequentType = EnumUtils.from(FrequentType.class, request.getFrequentType());
        recurringTransaction.setFrequentType(frequentType);
        recurringTransaction.setNextDate(frequentType.calculateNextDate(recurringTransaction.getStartDate()));

        RecurringTransaction updatedTransaction = recurringTransactionRepository.save(recurringTransaction);
        log.info("Successfully updated recurring transaction with ID: {}", updatedTransaction.getId());
        return recurringTransactionMapper.toResponse(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Attempting to delete recurring transaction with ID: {}", id);
        RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .orElseThrow(() -> {
                    log.warn("Recurring transaction with ID: {} not found for current user for deletion.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });
        recurringTransactionRepository.deleteById(recurringTransaction.getId());
        log.info("Successfully deleted recurring transaction with ID: {}", id);
    }
}
