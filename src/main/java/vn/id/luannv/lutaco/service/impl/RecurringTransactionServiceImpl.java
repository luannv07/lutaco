package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.entity.RecurringTransaction;
import vn.id.luannv.lutaco.service.RecurringTransactionService;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    @Override
    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public boolean processOne(RecurringTransaction rt) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public RecurringTransactionResponse getDetail(Long id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Page<RecurringTransactionResponse> search(RecurringTransactionFilterRequest request, Integer page, Integer size) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
