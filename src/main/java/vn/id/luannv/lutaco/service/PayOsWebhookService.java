package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.PayOsWebhookRequest;

public interface PayOsWebhookService {
    void handle(PayOsWebhookRequest request);
}
