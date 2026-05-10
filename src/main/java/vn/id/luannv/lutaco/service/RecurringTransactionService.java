package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionUpdateRequest;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;

import java.util.List;

public interface RecurringTransactionService
        extends BaseService<RecurringTransactionFilterRequest, RecurringTransactionResponse, RecurringTransactionRequest, Long> {

    RecurringTransactionResponse update(Long id, RecurringTransactionUpdateRequest request);

    void toggle(Long id);

    List<RecurringTransactionResponse> getMyRecurring();

    void executeJobById(Long jobId);
}
