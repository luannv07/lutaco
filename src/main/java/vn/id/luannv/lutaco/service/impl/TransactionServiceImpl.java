package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.TransactionMapper;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.TransactionService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    TransactionRepository transactionRepository;
    TransactionMapper transactionMapper;
    CategoryRepository categoryRepository;
    WalletRepository walletRepository;

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        log.info("TransactionServiceImpl create: {}", request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Wallet wallet = walletRepository.findByUser_IdAndId(SecurityUtils.getCurrentId(), request.getWalletId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Transaction transaction = transactionMapper.toEntity(request);

        transaction.setCategory(category);
        transaction.setUserId(SecurityUtils.getCurrentId());
        transaction.setWallet(wallet);
        applyBalance(wallet.getId(), transaction.getAmount(), category.getCategoryType());

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    private void applyBalance(String walletId, Long amount, CategoryType type) {
        if (type == null)
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        walletRepository.updateBalance(walletId, amount, type.name());

        log.info("TransactionServiceImpl amount & type: {} {}", amount, type);
    }

    @Override
    public TransactionResponse getDetail(String id) {
        log.info("TransactionServiceImpl getDetail: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> search(TransactionFilterRequest request, Integer page, Integer size) {
        log.info("TransactionServiceImpl search: {}", request);

        Pageable pageable = PageRequest.of(page - 1, size);
        return transactionRepository
                .findByFilters(request, SecurityUtils.getCurrentId(), pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional
    public TransactionResponse update(String id, TransactionRequest request) {
        log.info("TransactionServiceImpl update: {}, {}", id, request);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (request.getCategoryId() != null) {
            // lấy category mới
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            // lấy category hiện tại
            Object cateTypeObj = transactionRepository.findCategoryTypeById(id);
            CategoryType currentCategoryType = CategoryType.from(cateTypeObj);

            if (currentCategoryType == null || category.getCategoryType() == null)
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);

            Wallet wallet = transactionRepository.findWalletWithTransactionId(id)
                            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

            Long amountToApply = currentCategoryType == category.getCategoryType() ?
                    Math.abs(request.getAmount() - transaction.getAmount()) :
                    request.getAmount() + transaction.getAmount();

            applyBalance(wallet.getId(), amountToApply, category.getCategoryType());

            transaction.setCategory(category);
        }

        transactionMapper.updateEntity(transaction, request);

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException(ErrorCode.UNSUPPORTED_YET.getMessage());
    }

    @Override
    @Transactional
    public void deleteByIdAndWalletId(String transactionId, String walletId) {
        log.info("TransactionServiceImpl deleteById: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUserId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        transaction.setDeletedAt(LocalDateTime.now());
        Object cateTypeObj = transactionRepository.findCategoryTypeById(transactionId);
        if (cateTypeObj == null)
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);

        CategoryType reverse = reverseCategory(CategoryType.from(cateTypeObj));

        applyBalance(walletId, transaction.getAmount(), reverse);
        transactionRepository.save(transaction);
    }
    private CategoryType reverseCategory(CategoryType categoryType) {
        return categoryType == CategoryType.EXPENSE
                ? CategoryType.INCOME : CategoryType.EXPENSE;
    }
    @Override
    @Transactional
    public void restoreTransaction(String id, String walletId) {
        log.info("TransactionServiceImpl restoreTransaction: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() != null)
                .filter(t -> t.getUserId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Object cateTypeObj = transactionRepository.findCategoryTypeById(id);
        if (cateTypeObj == null)
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);

        transaction.setDeletedAt(null);
        transactionRepository.save(transaction);
        applyBalance(walletId, transaction.getAmount(), CategoryType.from(cateTypeObj));
    }
}
