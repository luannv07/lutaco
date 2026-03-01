package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        log.warn("[{}]: Method 'create(TransactionRequest request)' is not directly supported. Use 'customCreate' or 'createBulk'.", SecurityUtils.getCurrentUsername());
        return null; // Or throw an UnsupportedOperationException
    }

    @Override
    @Transactional
    @CachePut(value = "wallets", key = "#result.walletId + @securityPermission.getCurrentUserId()")
    @CacheEvict(value = "walletsList", key = "@securityPermission.getCurrentUserId()")
    public TransactionResponse customCreate(TransactionRequest request, String userId) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to create a custom transaction for user ID: {}. Request: {}", username, userId, request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("[{}]: Category with ID {} not found for transaction creation.", username, request.getCategoryId());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                });

        Wallet wallet = walletRepository.findByUser_IdAndId(userId, request.getWalletId())
                .orElseThrow(() -> {
                    log.warn("[{}]: Wallet with ID {} not found for user ID {} for transaction creation.", username, request.getWalletId(), userId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("walletId", request.getWalletId()));
                });

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setCategory(category);
        transaction.setUserId(userId);
        transaction.setWallet(wallet);

        applyBalance(wallet.getId(), transaction.getAmount(), category.getCategoryType());

        Transaction savedTransaction = transactionRepository.save(transaction);
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, savedTransaction));
        log.info("[{}]: Custom transaction (ID: {}) created successfully for user ID {}. Amount: {}, Category: {}, Wallet: {}.",
                username, savedTransaction.getId(), userId, savedTransaction.getAmount(), category.getCategoryName(), wallet.getWalletName());

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "wallets", allEntries = true),
            @CacheEvict(value = "walletsList", key = "#userId")
    })
    public List<TransactionResponse> createBulk(List<TransactionRequest> requests, String userId) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to create {} bulk transactions for user ID: {}.", username, requests.size(), userId);

        Set<Transaction> transactionsToSave = new HashSet<>();
        for (TransactionRequest request : requests) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("[{}]: Category with ID {} not found for bulk transaction creation.", username, request.getCategoryId());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                    });

            Wallet wallet = walletRepository.findByUser_IdAndId(userId, request.getWalletId())
                    .orElseThrow(() -> {
                        log.warn("[{}]: Wallet with ID {} not found for user ID {} for bulk transaction creation.", username, request.getWalletId(), userId);
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
        log.info("[{}]: Successfully created {} bulk transactions for user ID {}.", username, savedTransactions.size(), userId);

        return savedTransactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "wallets", allEntries = true),
            @CacheEvict(value = "walletsList", key = "#currentId")
    })
    public void deleteBulk(List<String> ids, String currentId) {
        String username = SecurityUtils.getCurrentUsername();
        Set<Transaction> transactionsToSave = new HashSet<>();
        for (String id : ids) {
            transactionRepository.findById(id).ifPresent(transaction -> {
                transaction.setDeletedAt(LocalDateTime.now());
                transactionsToSave.add(transaction);
            });
        }
        transactionRepository.saveAll(transactionsToSave);
        transactionsToSave.forEach(t -> eventPublisher.publishEvent(new TransactionDeletedEvent(this, t)));
        log.info("[{}]: Successfully deleted {} bulk transactions.", username, transactionsToSave.size());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "wallets", allEntries = true),
            @CacheEvict(value = "walletsList", key = "#userId")
    })
    public void autoCreateTransactionWithCronJob(String transactionId, String userId) {
        log.info("[system]: Auto-creating transaction via cron job for recurring transaction ID: {} for user ID: {}.", transactionId, userId);

        Transaction currentTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("[system]: Original transaction with ID {} not found for auto-creation.", transactionId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", transactionId));
                });

        RecurringTransactionProjection projection = transactionRepository.findLinkingFieldsById(currentTransaction.getId());
        if (projection == null) {
            log.error("[system]: Recurring transaction projection not found for transaction ID: {}.", currentTransaction.getId());
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", currentTransaction.getId()));
        }

        Category category = categoryRepository.findById(projection.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("[system]: Category with ID {} not found for auto-created transaction.", projection.getCategoryId());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", projection.getCategoryId()));
                });
        Wallet wallet = walletRepository.findById(projection.getWalletId())
                .orElseThrow(() -> {
                    log.warn("[system]: Wallet with ID {} not found for auto-created transaction.", projection.getWalletId());
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
        log.info("[system]: Transaction (ID: {}) auto-created successfully for recurring transaction ID: {}.", savedTransaction.getId(), transactionId);
    }

    private void applyBalance(String walletId, Long amount, CategoryType type) {
        if (type == null) {
            log.error("[system]: Category type is null when attempting to apply balance for wallet ID: {}.", walletId);
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        walletRepository.updateBalance(walletId, amount, type.name());
        log.debug("[system]: Balance updated for wallet ID: {}. Amount: {}, Type: {}.", walletId, amount, type);
    }

    @Override
    @Cacheable(value = "transactions",
            key = "#id + @securityPermission.getCurrentUserId()")
    public TransactionResponse getDetail(String id) {
        String username = SecurityUtils.getCurrentUsername();
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("[{}]: Fetching details for transaction ID: {} for user ID: {}.", username, id, currentUserId);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("[{}]: Transaction with ID {} not found or not accessible for user ID {}.", username, id, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                });

        log.info("[{}]: Successfully retrieved details for transaction ID {}.", username, id);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> search(TransactionFilterRequest request, Integer page, Integer size) {
        String username = SecurityUtils.getCurrentUsername();
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("[{}]: Searching transactions for user ID: {} with filter: {}, page: {}, size: {}.", username, currentUserId, request, page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TransactionResponse> result = transactionRepository
                .findByFilters(request, currentUserId, pageable)
                .map(transactionMapper::toResponse);
        log.info("[{}]: Found {} transactions matching the criteria for user ID {}.", username, result.getTotalElements(), currentUserId);
        return result;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "transactions", key = "#id + @securityPermission.getCurrentUserId()"),
            @CacheEvict(value = "wallets", key = "#request.walletId + @securityPermission.getCurrentUserId()"),
            @CacheEvict(value = "walletsList", key = "@securityPermission.getCurrentUserId()")
    })
    public TransactionResponse update(String id, TransactionRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("[{}]: Updating transaction ID: {} for user ID: {}. Request: {}", username, id, currentUserId, request);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("[{}]: Transaction with ID {} not found or not accessible for user ID {} for update.", username, id, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                });

        if (request.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("[{}]: New category with ID {} not found for transaction update.", username, request.getCategoryId());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
                    });

            Object cateTypeObj = transactionRepository.findCategoryTypeById(id);
            if (cateTypeObj == null) {
                log.error("[{}]: Category type not found for existing transaction ID {}.", username, id);
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
            }
            CategoryType currentCategoryType = EnumUtils.from(CategoryType.class, cateTypeObj);

            if (newCategory.getCategoryType() == null) {
                log.error("[{}]: New category with ID {} has a null category type.", username, request.getCategoryId());
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("categoryId", request.getCategoryId()));
            }

            Wallet wallet = transactionRepository.findWalletWithTransactionId(id)
                    .orElseThrow(() -> {
                        log.error("[{}]: Wallet not found for transaction ID {}.", username, id);
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                    });

            Long amountToApply = (currentCategoryType == newCategory.getCategoryType()) ?
                    Math.abs(request.getAmount() - transaction.getAmount()) :
                    request.getAmount() + transaction.getAmount();

            applyBalance(wallet.getId(), amountToApply, newCategory.getCategoryType());
            transaction.setCategory(newCategory);
            log.debug("[{}]: Transaction ID {} category updated to {} and balance adjusted.", username, id, newCategory.getCategoryName());
        }

        transactionMapper.updateEntity(transaction, request);
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("[{}]: Transaction ID {} updated successfully for user ID {}.", username, id, currentUserId);
        return transactionMapper.toResponse(updatedTransaction);
    }

    @Override
    public void deleteById(String id) {
        log.warn("[{}]: Direct deletion of transaction by ID {} is not supported. Use 'deleteByIdAndWalletId' for soft deletion.", SecurityUtils.getCurrentUsername(), id);
        throw new UnsupportedOperationException(ErrorCode.UNSUPPORTED_YET.getMessage());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "transactions", key = "#transactionId + @securityPermission.getCurrentUserId()"),
            @CacheEvict(value = "wallets", key = "#walletId + @securityPermission.getCurrentUserId()"),
            @CacheEvict(value = "walletsList", key = "@securityPermission.getCurrentUserId()")
    })
    public void deleteByIdAndWalletId(String transactionId, String walletId) {
        String username = SecurityUtils.getCurrentUsername();
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("[{}]: Attempting to soft delete transaction ID: {} from wallet ID: {} for user ID: {}.", username, transactionId, walletId, currentUserId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("[{}]: Transaction with ID {} not found or already deleted/inaccessible for user ID {} for soft deletion.", username, transactionId, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", transactionId));
                });

        transaction.setDeletedAt(LocalDateTime.now());
        Object cateTypeObj = transactionRepository.findCategoryTypeById(transactionId);
        if (cateTypeObj == null) {
            log.error("[{}]: Category type not found for transaction ID {} during soft deletion.", username, transactionId);
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", transactionId));
        }

        CategoryType reverseType = reverseCategory(EnumUtils.from(CategoryType.class, cateTypeObj));
        applyBalance(walletId, transaction.getAmount(), reverseType);
        Transaction savedTransaction = transactionRepository.save(transaction);

        eventPublisher.publishEvent(new TransactionDeletedEvent(this, savedTransaction));
        log.info("[{}]: Transaction ID {} soft deleted successfully for user ID {}. Balance adjusted for wallet ID {}.", username, transactionId, currentUserId, walletId);
    }

    private CategoryType reverseCategory(CategoryType categoryType) {
        return categoryType == CategoryType.EXPENSE
                ? CategoryType.INCOME : CategoryType.EXPENSE;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "transactions", key = "#id + @securityPermission.getCurrentUserId()"),
            @CacheEvict(value = "wallets", key = "#walletId + @securityPermission.getCurrentUserId()"),
            @CacheEvict(value = "walletsList", key = "@securityPermission.getCurrentUserId()")
    })
    public void restoreTransaction(String id, String walletId) {
        String username = SecurityUtils.getCurrentUsername();
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("[{}]: Attempting to restore transaction ID: {} to wallet ID: {} for user ID: {}.", username, id, walletId, currentUserId);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() != null)
                .filter(t -> t.getUserId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("[{}]: Transaction with ID {} not found or not soft deleted/inaccessible for user ID {} for restoration.", username, id, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
                });

        Object cateTypeObj = transactionRepository.findCategoryTypeById(id);
        if (cateTypeObj == null) {
            log.error("[{}]: Category type not found for transaction ID {} during restoration.", username, id);
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("transactionId", id));
        }

        transaction.setDeletedAt(null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        applyBalance(walletId, transaction.getAmount(), EnumUtils.from(CategoryType.class, cateTypeObj));

        eventPublisher.publishEvent(new TransactionCreatedEvent(this, savedTransaction));
        log.info("[{}]: Transaction ID {} restored successfully for user ID {}. Balance adjusted for wallet ID {}.", username, id, currentUserId, walletId);
    }
}
