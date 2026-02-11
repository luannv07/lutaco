package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.entity.RecurringTransaction;

public interface RecurringTransactionService extends
        BaseService<RecurringTransactionFilterRequest, RecurringTransactionResponse, RecurringTransactionRequest, Long> {
    void processOne(RecurringTransaction request);
}
