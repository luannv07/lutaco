package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;

public interface PayOsService {
    PayOSResponse createPayment(PaymentType paymentType);
}
