package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionUpdateRequest;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.RecurringTransaction;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.event.entity.RecurringExecutedEvent;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.RecurringTransactionRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.RecurringTransactionService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;
import vn.id.luannv.lutaco.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    RecurringTransactionRepository recurringTransactionRepository;
    TransactionRepository transactionRepository;
    WalletRepository walletRepository;
    CategoryRepository categoryRepository;
    UserRepository userRepository;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request) {
        Long userId = SecurityUtils.getCurrentId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "user.id")));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "category.id")));

        if (!category.isActiveFlg()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, Map.of("reason", "recurring.category.inactive"));
        }

        Wallet wallet = walletRepository.findByIdAndUserId(request.getWalletId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_ACCESS_DENIED));

        if (!wallet.isActiveFlg()) {
            throw new BusinessException(ErrorCode.WALLET_INACTIVE);
        }

        FrequentType frequentType = EnumUtils.from(FrequentType.class, request.getFrequentType());

        if (request.getEndDate() != null && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMS, Map.of("field", "endDate", "reason", "recurring.date.end_before_start"));
        }

        // Create template transaction (inactive - used only as metadata template)
        Transaction template = new Transaction();
        template.setUser(user);
        template.setCategory(category);
        template.setWallet(wallet);
        template.setAmount(request.getAmount());
        template.setNote(request.getNote());
        template.setTransactionDate(request.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        template.setActiveFlg(false);
        Transaction savedTemplate = transactionRepository.save(template);

        RecurringTransaction job = new RecurringTransaction();
        job.setTransaction(savedTemplate);
        job.setStartDate(request.getStartDate());
        job.setNextDate(request.getStartDate());
        job.setFrequentType(frequentType);
        job.setEndDate(request.getEndDate());
        job.setActiveFlg(true);
        recurringTransactionRepository.save(job);

        log.info("Recurring job created for user {}: category={}, wallet={}, freq={}",
                user.getUsername(), category.getId(), wallet.getId(), frequentType);
        return toResponse(job);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) {
        throw new UnsupportedOperationException("Use update(Long, RecurringTransactionUpdateRequest) instead.");
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentId();
        RecurringTransaction job = getMyJobOrThrow(id, userId);
        Transaction template = job.getTransaction();

        if (request.getAmount() != null) {
            template.setAmount(request.getAmount());
        }
        if (StringUtils.hasText(request.getNote())) {
            template.setNote(request.getNote());
        }
        if (StringUtils.hasText(request.getFrequentType())) {
            job.setFrequentType(EnumUtils.from(FrequentType.class, request.getFrequentType()));
        }
        if (request.getEndDate() != null) {
            if (!request.getEndDate().isAfter(job.getStartDate())) {
                throw new BusinessException(ErrorCode.INVALID_PARAMS, Map.of("field", "endDate", "reason", "recurring.date.end_before_start"));
            }
            job.setEndDate(request.getEndDate());
        }
        if (request.getNextDate() != null) {
            if (request.getNextDate().isBefore(LocalDate.now())) {
                throw new BusinessException(ErrorCode.INVALID_PARAMS, Map.of("field", "nextDate", "reason", "recurring.date.next_in_past"));
            }
            job.setNextDate(request.getNextDate());
        }

        transactionRepository.save(template);
        recurringTransactionRepository.save(job);
        return toResponse(job);
    }

    @Override
    @Transactional
    public void toggle(Long id) {
        Long userId = SecurityUtils.getCurrentId();
        RecurringTransaction job = getMyJobOrThrow(id, userId);
        job.setActiveFlg(!job.isActiveFlg());
        recurringTransactionRepository.save(job);
        log.info("Recurring job {} toggled to activeFlg={} by user {}", id, job.isActiveFlg(), userId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Long userId = SecurityUtils.getCurrentId();
        RecurringTransaction job = getMyJobOrThrow(id, userId);
        Transaction template = job.getTransaction();
        // Delete recurring job first, then the template transaction (inactive metadata)
        recurringTransactionRepository.delete(job);
        transactionRepository.delete(template);
        log.info("Recurring job {} and its template transaction deleted by user {}", id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringTransactionResponse getDetail(Long id) {
        Long userId = SecurityUtils.getCurrentId();
        return toResponse(getMyJobOrThrow(id, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecurringTransactionResponse> search(RecurringTransactionFilterRequest request, Integer page, Integer size) {
        Long userId = SecurityUtils.getCurrentId();
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Order.desc("createdDate")));

        Specification<RecurringTransaction> spec = (root, query, cb) ->
                cb.equal(root.get("transaction").get("user").get("id"), userId);

        if (StringUtils.hasText(request.getFrequentType())) {
            try {
                FrequentType ft = FrequentType.valueOf(request.getFrequentType().toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("frequentType"), ft));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid frequentType filter: {}", request.getFrequentType());
            }
        }

        return recurringTransactionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getMyRecurring() {
        return recurringTransactionRepository.findByUserId(SecurityUtils.getCurrentId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void executeJobById(Long jobId) {
        RecurringTransaction job = recurringTransactionRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", jobId)));

        if (!job.isActiveFlg()) {
            log.info("[recurring-job]: Job {} is inactive, skipping.", jobId);
            return;
        }

        LocalDate today = LocalDate.now();

        // Deactivate expired jobs
        if (job.getEndDate() != null && job.getNextDate().isAfter(job.getEndDate())) {
            job.setActiveFlg(false);
            recurringTransactionRepository.save(job);
            log.info("[recurring-job]: Job {} has expired (endDate={}), deactivated.", jobId, job.getEndDate());
            return;
        }

        Transaction template = job.getTransaction();
        Category category = template.getCategory();
        Wallet wallet = template.getWallet();
        User user = template.getUser();

        // Validate wallet is still active
        if (!wallet.isActiveFlg()) {
            log.warn("[recurring-job]: Job {} skipped — wallet {} is inactive.", jobId, wallet.getId());
            return;
        }

        // Create a real transaction from the template
        Instant txDate = job.getNextDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Transaction newTx = new Transaction();
        newTx.setUser(user);
        newTx.setCategory(category);
        newTx.setWallet(wallet);
        newTx.setAmount(template.getAmount());
        newTx.setNote(template.getNote());
        newTx.setTransactionDate(txDate);
        newTx.setActiveFlg(true);

        // Update wallet balance atomically
        String categoryType = category.getCategoryType().name();
        int updated = walletRepository.updateBalance(wallet.getId(), template.getAmount(), categoryType);
        if (updated == 0) {
            log.warn("[recurring-job]: Job {} — insufficient balance in wallet {}. Will retry on next cycle.",
                    jobId, wallet.getId());
            return;
        }

        Transaction savedTx = transactionRepository.save(newTx);

        // Advance nextDate for next execution
        LocalDate newNextDate = job.getFrequentType().calculateNextDate(job.getNextDate());
        job.setNextDate(newNextDate);
        recurringTransactionRepository.save(job);

        log.info("[recurring-job]: Job {} executed successfully. generatedTxId={}, nextDate={}",
                jobId, savedTx.getId(), newNextDate);

        eventPublisher.publishEvent(RecurringExecutedEvent.builder()
                .recurringJobId(jobId)
                .generatedTransactionId(savedTx.getId())
                .userId(user.getId())
                .categoryType(categoryType)
                .build());
    }

    private RecurringTransaction getMyJobOrThrow(Long id, Long userId) {
        return recurringTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));
    }

    private RecurringTransactionResponse toResponse(RecurringTransaction job) {
        if (job == null) return null;
        Transaction t = job.getTransaction();
        return RecurringTransactionResponse.builder()
                .id(job.getId())
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getCategoryCode() : null)
                .categoryType(t.getCategory() != null ? t.getCategory().getCategoryType() : null)
                .walletId(t.getWallet() != null ? t.getWallet().getId() : null)
                .walletName(t.getWallet() != null ? t.getWallet().getName() : null)
                .amount(t.getAmount())
                .note(t.getNote())
                .frequentType(job.getFrequentType())
                .startDate(job.getStartDate())
                .nextDate(job.getNextDate())
                .endDate(job.getEndDate())
                .activeFlg(job.isActiveFlg())
                .build();
    }
}
