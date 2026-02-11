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
        log.info("RecurringTransactionServiceImpl create: {}", request);
        InternalState state = createAndSaveTransaction(request);
        publishEvent(state, RecurringTransactionEvent.RecurringTransactionState.INITIALIZER);
        return recurringTransactionMapper.toResponse(state.recurringTransaction());
    }

    @Transactional
    public void createWithCronJob(RecurringTransactionRequest request) {
        log.info("RecurringTransactionServiceImpl createWithCronJob: {}", request);
        InternalState state = createAndSaveTransaction(request);
        publishEvent(state, RecurringTransactionEvent.RecurringTransactionState.FREQUENCY);
    }

    @Override
    @Transactional
    public void processOne(RecurringTransaction rt) {
        log.info("processOne {}", rt);
        RecurringTransactionEvent.RecurringUserFields recurringUserFields = transactionRepository
                .getRecurringUserFieldsByTransactionId(rt.getTransaction().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        InternalState state = new InternalState(rt, recurringUserFields);
        publishEvent(state, RecurringTransactionEvent.RecurringTransactionState.FREQUENCY);

        rt.setNextDate(rt.getFrequentType().calculateNextDate(LocalDate.now()));
        recurringTransactionRepository.save(rt);
    }

    private InternalState createAndSaveTransaction(RecurringTransactionRequest request) {
        RecurringTransactionEvent.RecurringUserFields recurringUserFields = transactionRepository
                .getRecurringUserFieldsByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Transaction transaction = transactionRepository.getReferenceById(request.getTransactionId());
        RecurringTransaction recurringTransaction = recurringTransactionMapper.toEntity(request);
        recurringTransaction.setTransaction(transaction);
        FrequentType frequentType = FrequentType.from(request.getFrequentType());
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
    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) {
        log.info("RecurringTransactionServiceImpl update: {}, {}", id, request);

        RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        recurringTransactionMapper.updateEntity(recurringTransaction, request);
        FrequentType frequentType = FrequentType.from(request.getFrequentType());
        recurringTransaction.setFrequentType(frequentType);
        recurringTransaction.setNextDate(frequentType.calculateNextDate(recurringTransaction.getStartDate()));


        return recurringTransactionMapper.toResponse(
                recurringTransactionRepository.save(recurringTransaction)
        );
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("RecurringTransactionServiceImpl deleteById: {}", id);
        RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserIdAndId(SecurityUtils.getCurrentId(), id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        recurringTransactionRepository.deleteById(recurringTransaction.getId());
    }
}
