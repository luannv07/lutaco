package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.service.PayOsService;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class PayOsServiceImpl implements PayOsService {

    @Override
    public PayOSResponse<PayOSResponse.PayOSDataCreated> createPayment(PaymentType paymentType) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public PayOSResponse<PayOSResponse.PayOSDataDetail> getDetailPayment(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public List<PayOSResponse.PayOSDataByUser> getPaymentsByUserId(String userId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
