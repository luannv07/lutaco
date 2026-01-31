package vn.id.luannv.lutaco.service.impl;

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
import vn.id.luannv.lutaco.enumerate.TransactionType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.TransactionMapper;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.repository.TransactionRepository;
import vn.id.luannv.lutaco.service.TransactionService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    TransactionRepository transactionRepository;
    CategoryRepository categoryRepository;
    TransactionMapper transactionMapper;

    @Override
    public TransactionResponse create(TransactionRequest request) {
        log.info("TransactionServiceImpl create: {}", request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Transaction transaction = transactionMapper.toEntity(request);

        if (request.getTransactionType() != null &&
            TransactionType.isValidTransactionType(request.getTransactionType()))
            transaction.setTransactionType(TransactionType.valueOf(request.getTransactionType()));

        transaction.setCategory(category);
        transaction.setUser(User.builder().id(SecurityUtils.getCurrentId()).build());

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    @Override
    public TransactionResponse getDetail(String id) {
        log.info("TransactionServiceImpl getDetail: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUser().getId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> search(TransactionFilterRequest request, Integer page, Integer size) {
        log.info("TransactionServiceImpl search: {}", request);

        Pageable pageable = PageRequest.of(page - 1, size);

        if (request.getTransactionType() != null
                && !TransactionType.isValidTransactionType(request.getTransactionType())) {
            request.setTransactionType(null);
        }

        return transactionRepository
                .findByFilters(request, SecurityUtils.getCurrentId(), pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    public TransactionResponse update(String id, TransactionRequest request) {
        log.info("TransactionServiceImpl update: {}, {}", id, request);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUser().getId().equals(SecurityUtils.getCurrentId()))
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
        log.info("TransactionServiceImpl deleteById: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .filter(t -> t.getUser().getId().equals(SecurityUtils.getCurrentId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        transaction.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
