package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.service.PayOsClient;
import vn.id.luannv.lutaco.service.PayOsService;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class PayOsServiceImpl implements PayOsService {
    PayOsClient payOsClient;

    @Override
    public PayOSResponse<PayOSResponse.PayOSDataCreated> createPayment(PaymentType paymentType) {
        return payOsClient.createPayment(paymentType);
    }

    @Override
    @Cacheable(value = "payos", key = "#id")
    public PayOSResponse<PayOSResponse.PayOSDataDetail> getDetailPayment(Integer id) {
        return payOsClient.getDetail(id);
    }

    @Override
    public List<PayOSResponse.PayOSDataByUser> getPaymentsByUserId(Long userId) {
        return payOsClient.getBySelf(userId);
    }
}
