package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.PayOsWebhookRequest;
import vn.id.luannv.lutaco.service.PayOsWebhookService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PayOsWebhookServiceImpl implements PayOsWebhookService {

    @Override
    @Transactional
    public void handle(PayOsWebhookRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
