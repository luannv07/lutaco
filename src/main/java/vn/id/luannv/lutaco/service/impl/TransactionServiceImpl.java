package vn.id.luannv.lutaco.service.impl;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.event.entity.TransactionChangedEvent;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.TransactionService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vn.id.luannv.lutaco.enumerate.CategoryType.EXPENSE;
import static vn.id.luannv.lutaco.enumerate.CategoryType.INCOME;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {
    TransactionRepository transactionRepository;
    WalletRepository walletRepository;
    CategoryRepository categoryRepository;
    UserRepository userRepository;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public List<TransactionResponse> createBulk(List<TransactionRequest> requests, Long userId) {
        List<TransactionResponse> responses = new ArrayList<>();
        for (TransactionRequest request : requests) {
            try {
                responses.add(createTransaction(request, userId));
            } catch (Exception e) {
                log.error("Error creating transaction for user {}: {}", userId, e.getMessage());
            }
        }
        return responses;
    }

    private Specification<Transaction> buildSpec(TransactionFilterRequest req, Long userId) {
        return (root, query, cb) -> {

            // tránh duplicate khi join
            if (Transaction.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("wallet", JoinType.LEFT);
                query.distinct(true);
            }

            Predicate predicate = cb.conjunction();

            // luôn filter theo user
            predicate = cb.and(predicate,
                    cb.equal(root.get("user").get("id"), userId)
            );

            if (req.getCategoryId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("category").get("id"), req.getCategoryId())
                );
            }

            if (req.getWalletId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("wallet").get("id"), req.getWalletId())
                );
            }

            if (req.getFromDate() != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("transactionDate"), req.getFromDate())
                );
            }

            if (req.getToDate() != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("transactionDate"), req.getToDate())
                );
            }

            if (req.getMinAmount() != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("amount"), req.getMinAmount())
                );
            }

            if (req.getMaxAmount() != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("amount"), req.getMaxAmount())
                );
            }

            // activeFlg
            predicate = cb.and(predicate,
                    cb.isTrue(root.get("activeFlg"))
            );

            return predicate;
        };
    }

    @Override
    @Transactional
    public void deleteBulk(List<Long> ids, Long userId) {
        List<Transaction> transactions = transactionRepository.findAllById(ids);
        
        for (Transaction transaction : transactions) {
            if (!transaction.getUser().getId().equals(userId)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            
            if (!transaction.isActiveFlg()) {
                continue;
            }
            
            // Revert wallet balance
            String categoryType = transaction.getCategory().getCategoryType().name();
            walletRepository.updateBalance(
                    transaction.getWallet().getId(),
                    transaction.getAmount(),
                    categoryType.equals("EXPENSE") ? "INCOME" : "EXPENSE"
            );
            
            // Soft delete
            transaction.setActiveFlg(false);
            transactionRepository.save(transaction);
            publishTransactionChangedEvent(transaction);
            log.info("Transaction {} deleted for user {}", transaction.getId(), userId);
        }
    }

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentId();
        return createTransaction(request, userId);
    }

    private TransactionResponse createTransaction(TransactionRequest request, Long userId) {
        // Validate and fetch wallet
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "wallet.id")));
        
        if (!wallet.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // Validate and fetch category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "category.id")));
        
        // Fetch user
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "user.id")));
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setWallet(wallet);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNote(request.getNote());
        transaction.setActiveFlg(true);
        
        // Update wallet balance
        String categoryType = category.getCategoryType().name();
        int updated = walletRepository.updateBalance(
                wallet.getId(),
                request.getAmount(),
                categoryType
        );
        
        if (updated == 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, Map.of("message", "Insufficient balance or wallet not found"));
        }
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        publishTransactionChangedEvent(savedTransaction);
        log.info("Transaction created for user {}: {}", userId, savedTransaction.getId());
        
        return toResponse(savedTransaction);
    }

    @Override
    public TransactionResponse getDetail(Long id) {

        Long userId = SecurityUtils.getCurrentId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        return toResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> search(TransactionFilterRequest request) {

        Long userId = SecurityUtils.getCurrentId();

        Specification<Transaction> spec = buildSpec(request, userId);

        Page<Transaction> pageData = transactionRepository.findAll(spec, request.pageable());

        return pageData.map(this::toResponse);
    }

    @Override
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentId();
        
        // Fetch existing transaction
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "transaction.id")));
        
        // Validate new wallet if it changed
        Wallet newWallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "wallet.id")));
        
        if (!newWallet.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // Validate new category if it changed
        Category newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "category.id")));
        
        // Revert old transaction amount from wallet
        String oldCategoryType = transaction.getCategory().getCategoryType().name();
        walletRepository.updateBalance(
                transaction.getWallet().getId(),
                transaction.getAmount(),
                oldCategoryType.equals(EXPENSE.name()) ? INCOME.name() : EXPENSE.name()
        );
        
        // Update transaction fields
        transaction.setCategory(newCategory);
        transaction.setWallet(newWallet);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNote(request.getNote());
        
        // Apply new transaction amount to wallet
        String newCategoryType = newCategory.getCategoryType().name();
        int updated = walletRepository.updateBalance(
                newWallet.getId(),
                request.getAmount(),
                newCategoryType
        );
        
        if (updated == 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, Map.of("message", "Insufficient balance or wallet not found"));
        }
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        publishTransactionChangedEvent(updatedTransaction);
        log.info("Transaction {} updated for user {}", id, userId);
        
        return toResponse(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Long userId = SecurityUtils.getCurrentId();
        
        // Fetch transaction
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "transaction.id")));
        
        if (!transaction.isActiveFlg()) {
            log.warn("Transaction {} is already deleted", id);
            return;
        }
        
        // Revert wallet balance
        String categoryType = transaction.getCategory().getCategoryType().name();
        walletRepository.updateBalance(
                transaction.getWallet().getId(),
                transaction.getAmount(),
                categoryType.equals("EXPENSE") ? "INCOME" : "EXPENSE"
        );
        
        // Soft delete
        transaction.setActiveFlg(false);
        transactionRepository.save(transaction);
        publishTransactionChangedEvent(transaction);
        log.info("Transaction {} deleted for user {}", id, userId);
    }

    @Override
    @Transactional
    public void deleteByIdAndWalletId(Long id, Long walletId) {
        Long userId = SecurityUtils.getCurrentId();
        
        // Fetch transaction
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "transaction.id")));
        
        // Validate wallet matches
        if (!transaction.getWallet().getId().equals(walletId)) {
            throw new BusinessException(ErrorCode.WALLET_ACCESS_DENIED);
        }
        
        if (!transaction.isActiveFlg()) {
            log.warn("Transaction {} is already deleted", id);
            return;
        }
        
        // Revert wallet balance
        String categoryType = transaction.getCategory().getCategoryType().name();
        walletRepository.updateBalance(
                walletId,
                transaction.getAmount(),
                categoryType.equals("EXPENSE") ? "INCOME" : "EXPENSE"
        );
        
        // Soft delete
        transaction.setActiveFlg(false);
        transactionRepository.save(transaction);
        publishTransactionChangedEvent(transaction);
        log.info("Transaction {} deleted for wallet {} by user {}", id, walletId, userId);
    }

    @Override
    @Transactional
    public void restoreTransaction(Long id, Long walletId) {
        Long userId = SecurityUtils.getCurrentId();
        
        // Fetch transaction (including deleted ones)
        Transaction transaction = transactionRepository.findByIdAndUserIdIncludingDeleted(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("field", "transaction.id")));
        
        // Validate wallet matches
        if (!transaction.getWallet().getId().equals(walletId)) {
            throw new BusinessException(ErrorCode.WALLET_ACCESS_DENIED);
        }
        
        if (transaction.isActiveFlg()) {
            log.warn("Transaction {} is already active", id);
            return;
        }
        
        // Restore wallet balance
        String categoryType = transaction.getCategory().getCategoryType().name();
        walletRepository.updateBalance(
                walletId,
                transaction.getAmount(),
                categoryType
        );
        
        // Restore transaction
        transaction.setActiveFlg(true);
        transactionRepository.save(transaction);
        publishTransactionChangedEvent(transaction);
        log.info("Transaction {} restored for wallet {} by user {}", id, walletId, userId);
    }

    private void publishTransactionChangedEvent(Transaction transaction) {
        eventPublisher.publishEvent(TransactionChangedEvent.builder()
                .transactionId(transaction.getId())
                .userId(transaction.getUser().getId())
                .categoryId(transaction.getCategory().getId())
                .build());
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .categoryId(t.getCategory().getId())
                .categoryName(t.getCategory().getCategoryCode())
                .categoryType(t.getCategory().getCategoryType())
                .amount(t.getAmount())
                .transactionDate(t.getTransactionDate())
                .note(t.getNote())
                .createdDate(t.getCreatedDate())
                .walletId(t.getWallet().getId())
                .walletName(t.getWallet().getName())
                .build();
    }
}
