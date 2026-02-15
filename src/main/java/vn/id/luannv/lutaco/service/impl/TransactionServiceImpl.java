package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.projection.RecurringTransactionProjection;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.event.entity.TransactionCreatedEvent;
import vn.id.luannv.lutaco.event.entity.TransactionDeletedEvent;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.TransactionMapper;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.TransactionService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    TransactionRepository transactionRepository;
    TransactionMapper transactionMapper;
    CategoryRepository categoryRepository;
    WalletRepository walletRepository;
    ApplicationEventPublisher eventPublisher;

    @Override
    public TransactionResponse create(TransactionRequest request) {
        log.warn("Method 'create(TransactionRequest request)' is not directly supported. Use 'customCreate' or 'createBulk'.");
        return null; // Or throw an UnsupportedOperationException
    }

    @Override
    @Transactional
    @CacheEvict(value = {"transactions", "dashboardSummaries", "categoryExpenses"}, allEntries = true)
    public TransactionResponse customCreate(TransactionRequest request, String userId) {
        log.info("Attempting to create a custom transaction for user ID: {}. Request: {}", userId, request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category with ID {} not found for transaction creation.", request.getCategoryId());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                });

        Wallet wallet = walletRepository.findByUser_IdAndId(userId, request.getWalletId())
                .orElseThrow(() -> {
                    log.warn("Wallet with ID {} not found for user ID {} for transaction creation.", request.getWalletId(), userId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("walletId", request.getWalletId()));
                });

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setCategory(category);
        transaction.setUserId(userId);
        transaction.setWallet(wallet);

        applyBalance(wallet.getId(), transaction.getAmount(), category.getCategoryType());

        Transaction savedTransaction = transactionRepository.save(transaction);
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, savedTransaction));
        log.info("Custom transaction (ID: {}) created successfully for user ID {}. Amount: {}, Category: {}, Wallet: {}.",
                savedTransaction.getId(), userId, savedTransaction.getAmount(), category.getCategoryName(), wallet.getWalletName());

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"transactions", "dashboardSummaries", "categoryExpenses"}, allEntries = true)
    public List<TransactionResponse> createBulk(List<TransactionRequest> requests, String userId) {
        log.info("Attempting to create {} bulk transactions for user ID: {}.", requests.size(), userId);

        List<Transaction> transactionsToSave = new ArrayList<>();
        for (TransactionRequest request : requests) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("Category with ID {} not found for bulk transaction creation.", request.getCategoryId());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                    });

            Wallet wallet = walletRepository.findByUser_IdAndId(userId, request.getWalletId())
                    .orElseThrow(() -> {
                        log.warn("Wallet with ID {} not found for user ID {} for bulk transaction creation.", request.getWalletId(), userId);
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("walletId", request.getWalletId()));
                    });

            Transaction transaction = transactionMapper.toEntity(request);
            transaction.setCategory(category);
            transaction.setUserId(userId);
            transaction.setWallet(wallet);

            applyBalance(wallet.getId(), transaction.getAmount(), category.getCategoryType());
            transactionsToSave.add(transaction);
        }

        List<Transaction> savedTransactions = transactionRepository.saveAll(transactionsToSave);
        savedTransactions.forEach(t -> eventPublisher.publishEvent(new TransactionCreatedEvent(this, t)));
        log.info("Successfully created {} bulk transactions for user ID {}.", savedTransactions.size(), userId);

        return savedTransactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"transactions", "dashboardSummaries", "categoryExpenses"}, allEntries = true)
    public void autoCreateTransactionWithCronJob(String transactionId, String userId) {
        log.info("Auto-creating transaction via cron job for recurring transaction ID: {} for user ID: {}.", transactionId, userId);

        Transaction currentTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Original transaction with ID {} not found for auto-creation.", transactionId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", transactionId));
                });

        RecurringTransactionProjection projection = transactionRepository.findLinkingFieldsById(currentTransaction.getId());
        if (projection == null) {
            log.error("Recurring transaction projection not found for transaction ID: {}.", currentTransaction.getId());
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", currentTransaction.getId()));
        }

        Category category = categoryRepository.findById(projection.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category with ID {} not found for auto-created transaction.", projection.getCategoryId());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", projection.getCategoryId()));
                });
        Wallet wallet = walletRepository.findById(projection.getWalletId())
                .orElseThrow(() -> {
                    log.warn("Wallet with ID {} not found for auto-created transaction.", projection.getWalletId());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("walletId", projection.getWalletId()));
                });

        Transaction entity = Transaction.builder()
                .userId(userId)
                .category(category)
                .wallet(wallet)
                .amount(currentTransaction.getAmount())
                .transactionDate(currentTransaction.getTransactionDate())
                .note(currentTransaction.getNote())
                .deletedAt(null)
                .build();

        CategoryType categoryType = EnumUtils.from(CategoryType.class, projection.getCategoryType());
        applyBalance(projection.getWalletId(), currentTransaction.getAmount(), categoryType);

        Transaction savedTransaction = transactionRepository.save(entity);
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, savedTransaction));
        log.info("Transaction (ID: {}) auto-created successfully for recurring transaction ID: {}.", savedTransaction.getId(), transactionId);
    }

    private void applyBalance(String walletId, Long amount, CategoryType type) {
        if (type == null) {
            log.error("Category type is null when attempting to apply balance for wallet ID: {}.", walletId);
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        walletRepository.updateBalance(walletId, amount, type.name());
        log.debug("Balance updated for wallet ID: {}. Amount: {}, Type: {}.", walletId, amount, type);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id")
    public TransactionResponse getDetail(String id) {
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("Fetching details for transaction ID: {} for user ID: {}.", id, currentUserId);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("Transaction with ID {} not found or not accessible for user ID {}.", id, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                });

        log.info("Successfully retrieved details for transaction ID {}.", id);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Cacheable(value = "transactions", key = "{#request, #page, #size}")
    public Page<TransactionResponse> search(TransactionFilterRequest request, Integer page, Integer size) {
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("Searching transactions for user ID: {} with filter: {}, page: {}, size: {}.", currentUserId, request, page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TransactionResponse> result = transactionRepository
                .findByFilters(request, currentUserId, pageable)
                .map(transactionMapper::toResponse);
        log.info("Found {} transactions matching the criteria for user ID {}.", result.getTotalElements(), currentUserId);
        return result;
    }

    @Override
    @Transactional
    @CachePut(value = "transactions", key = "#id")
    @CacheEvict(value = {"transactions", "dashboardSummaries", "categoryExpenses"}, allEntries = true)
    public TransactionResponse update(String id, TransactionRequest request) {
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("Updating transaction ID: {} for user ID: {}. Request: {}", id, currentUserId, request);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("Transaction with ID {} not found or not accessible for user ID {} for update.", id, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                });

        if (request.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("New category with ID {} not found for transaction update.", request.getCategoryId());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                    });

            Object cateTypeObj = transactionRepository.findCategoryTypeById(id);
            if (cateTypeObj == null) {
                log.error("Category type not found for existing transaction ID {}.", id);
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
            }
            CategoryType currentCategoryType = EnumUtils.from(CategoryType.class, cateTypeObj);

            if (newCategory.getCategoryType() == null) {
                log.error("New category with ID {} has a null category type.", request.getCategoryId());
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
            }

            Wallet wallet = transactionRepository.findWalletWithTransactionId(id)
                            .orElseThrow(() -> {
                                log.error("Wallet not found for transaction ID {}.", id);
                                return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                            });

            Long amountToApply = (currentCategoryType == newCategory.getCategoryType()) ?
                    Math.abs(request.getAmount() - transaction.getAmount()) :
                    request.getAmount() + transaction.getAmount();

            applyBalance(wallet.getId(), amountToApply, newCategory.getCategoryType());
            transaction.setCategory(newCategory);
            log.debug("Transaction ID {} category updated to {} and balance adjusted.", id, newCategory.getCategoryName());
        }

        transactionMapper.updateEntity(transaction, request);
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction ID {} updated successfully for user ID {}.", id, currentUserId);
        return transactionMapper.toResponse(updatedTransaction);
    }

    @Override
    public void deleteById(String id) {
        log.warn("Direct deletion of transaction by ID {} is not supported. Use 'deleteByIdAndWalletId' for soft deletion.", id);
        throw new UnsupportedOperationException(ErrorCode.UNSUPPORTED_YET.getMessage());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"transactions", "dashboardSummaries", "categoryExpenses"}, allEntries = true)
    public void deleteByIdAndWalletId(String transactionId, String walletId) {
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("Attempting to soft delete transaction ID: {} from wallet ID: {} for user ID: {}.", transactionId, walletId, currentUserId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("Transaction with ID {} not found or already deleted/inaccessible for user ID {} for soft deletion.", transactionId, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", transactionId));
                });

        transaction.setDeletedAt(LocalDateTime.now());
        Object cateTypeObj = transactionRepository.findCategoryTypeById(transactionId);
        if (cateTypeObj == null) {
            log.error("Category type not found for transaction ID {} during soft deletion.", transactionId);
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", transactionId));
        }

        CategoryType reverseType = reverseCategory(EnumUtils.from(CategoryType.class, cateTypeObj));
        applyBalance(walletId, transaction.getAmount(), reverseType);
        Transaction savedTransaction = transactionRepository.save(transaction);

        eventPublisher.publishEvent(new TransactionDeletedEvent(this, savedTransaction));
        log.info("Transaction ID {} soft deleted successfully for user ID {}. Balance adjusted for wallet ID {}.", transactionId, currentUserId, walletId);
    }
    private CategoryType reverseCategory(CategoryType categoryType) {
        return categoryType == CategoryType.EXPENSE
                ? CategoryType.INCOME : CategoryType.EXPENSE;
    }
    @Override
    @Transactional
    @CachePut(value = "transactions", key = "#id")
    @CacheEvict(value = {"transactions", "dashboardSummaries", "categoryExpenses"}, allEntries = true)
    public void restoreTransaction(String id, String walletId) {
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("Attempting to restore transaction ID: {} to wallet ID: {} for user ID: {}.", id, walletId, currentUserId);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() != null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("Transaction with ID {} not found or not soft deleted/inaccessible for user ID {} for restoration.", id, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                });

        Object cateTypeObj = transactionRepository.findCategoryTypeById(id);
        if (cateTypeObj == null) {
            log.error("Category type not found for transaction ID {} during restoration.", id);
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
        }

        transaction.setDeletedAt(null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        applyBalance(walletId, transaction.getAmount(), EnumUtils.from(CategoryType.class, cateTypeObj));

        eventPublisher.publishEvent(new TransactionCreatedEvent(this, savedTransaction));
        log.info("Transaction ID {} restored successfully for user ID {}. Balance adjusted for wallet ID {}.", id, currentUserId, walletId);
    }
}
