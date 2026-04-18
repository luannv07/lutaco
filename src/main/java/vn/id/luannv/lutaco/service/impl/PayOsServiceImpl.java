package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.entity.PayOS;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.PayOSRepository;
import vn.id.luannv.lutaco.service.PayOsClient;
import vn.id.luannv.lutaco.service.PayOsService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class PayOsServiceImpl implements PayOsService {
    PayOsClient payOsClient;
    PayOSRepository payOSRepository;

    @Override
    public PayOSResponse<PayOSResponse.PayOSDataCreated> createPayment(PaymentType paymentType) {
        return payOsClient.createPayment(paymentType);
    }

    @Override
    public PayOSResponse<PayOSResponse.PayOSDataDetail> getDetailPayment(String id) {
        return payOsClient.getDetail(id);
    }

    @Override
    public List<PayOSResponse.PayOSDataByUser> getPaymentsByUserId(String userId) {
        String currentUserId = SecurityUtils.getCurrentId();
        if (!currentUserId.equals(userId)) {
            log.warn("[{}]: Forbidden attempt to query PayOS transactions for user {}.", currentUserId, userId);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return payOSRepository.findByUser_IdOrderByCreatedDateDesc(userId)
                .stream()
                .map(this::toByUserResponse)
                .toList();
    }

    private PayOSResponse.PayOSDataByUser toByUserResponse(PayOS payOS) {
        return PayOSResponse.PayOSDataByUser.builder()
                .orderCode(payOS.getOrderCode())
                .paymentLinkId(payOS.getPaymentLinkId())
                .amount(payOS.getAmount())
                .currency(payOS.getCurrency())
                .description(payOS.getDescription())
                .status(payOS.getStatus().name())
                .type(payOS.getType().name())
                .createdDate(payOS.getCreatedDate())
                .paidAt(payOS.getPaidAt())
                .build();
    }
}
