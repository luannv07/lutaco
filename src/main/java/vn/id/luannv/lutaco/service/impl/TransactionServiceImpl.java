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
import vn.id.luannv.lutaco.enumerate.TransactionType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.TransactionMapper;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.TransactionService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDateTime;

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

        if (request.getTransactionType() != null &&
            TransactionType.isValidTransactionType(request.getTransactionType()))
            transaction.setTransactionType(TransactionType.valueOf(request.getTransactionType()));

        transaction.setCategory(category);
        transaction.setUserId(SecurityUtils.getCurrentId());
        transaction.setWallet(wallet);
        applyBalance(wallet.getId(), transaction.getAmount(), transaction.getTransactionType());

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    private void applyBalance(String walletId, Long amount, TransactionType type) {
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
                .filter(t -> t.getId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> search(TransactionFilterRequest request, Integer page, Integer size) {
        log.info("TransactionServiceImpl search: {}", request);

        Pageable pageable = PageRequest.of(page - 1, size);

        if (request.getTransactionType() != null
                && !TransactionType.isValidTransactionType(request.getTransactionType().name())) {
            request.setTransactionType(null);
        }
        log.info("Passed");

        return transactionRepository
                .findByFilters(request, SecurityUtils.getCurrentId(), pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    public TransactionResponse update(String id, TransactionRequest request) {
        log.info("TransactionServiceImpl update: {}, {}", id, request);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        transactionMapper.updateEntity(transaction, request);
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            transaction.setCategory(category);
        }

        if (request.getTransactionType() != null &&
                TransactionType.isValidTransactionType(request.getTransactionType()))
            transaction.setTransactionType(TransactionType.valueOf(request.getTransactionType()));

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
                .filter(t -> t.getId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        transaction.setDeletedAt(LocalDateTime.now());
        TransactionType reverse = transaction.getTransactionType() == TransactionType.EXPENSE ?
                TransactionType.INCOME :  TransactionType.EXPENSE;
        applyBalance(walletId, transaction.getAmount(), reverse);
        transactionRepository.save(transaction);
    }
}
