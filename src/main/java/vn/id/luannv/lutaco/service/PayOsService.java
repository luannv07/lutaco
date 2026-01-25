package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;

public interface PayOsService {
    PayOSResponse<PayOSResponse.PayOSDataCreated> createPayment(PaymentType paymentType);

    PayOSResponse<PayOSResponse.PayOSDataDetail> getDetailPayment(String id);
}
