package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;

public interface TransactionService extends
        BaseService<TransactionFilterRequest, TransactionResponse, TransactionRequest, String> {
    void deleteByIdAndWalletId(String transactionId, String walletId);
}
