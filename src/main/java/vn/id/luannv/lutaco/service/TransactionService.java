package vn.id.luannv.lutaco.service;

import jakarta.validation.Valid;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService extends
        BaseService<TransactionFilterRequest, TransactionResponse, TransactionRequest, Long> {
    List<TransactionResponse> createBulk(List<TransactionRequest> requests, Long currentId);

    void deleteBulk(@Valid List<Long> ids, Long currentId);

    void deleteByIdAndWalletId(Long id, Long walletId);

    void restoreTransaction(Long id, Long walletId);
}
