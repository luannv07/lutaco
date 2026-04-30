package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.service.TransactionService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    @Override
    public TransactionResponse create(TransactionRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public TransactionResponse customCreate(TransactionRequest request, String userId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public List<TransactionResponse> createBulk(List<TransactionRequest> requests, String userId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void deleteBulk(List<String> ids, String currentId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void autoCreateTransactionWithCronJob(String transactionId, String userId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public TransactionResponse getDetail(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Page<TransactionResponse> search(TransactionFilterRequest request, Integer page, Integer size) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public TransactionResponse update(String id, TransactionRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void deleteByIdAndWalletId(String transactionId, String walletId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void restoreTransaction(String id, String walletId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
