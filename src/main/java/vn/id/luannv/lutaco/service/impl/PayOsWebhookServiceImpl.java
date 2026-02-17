package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.PayOsWebhookRequest;
import vn.id.luannv.lutaco.entity.PayOS;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.PaymentStatus;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.PayOSRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.PayOsWebhookService;
import vn.id.luannv.lutaco.util.PayOsSignatureUtils;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PayOsWebhookServiceImpl implements PayOsWebhookService {
    PayOSRepository payOSRepository;
    UserRepository userRepository;

    @NonFinal
    @Value("${payment.check-sum-key}")
    String checkSumKey;

    @NonFinal
    @Value("${payment.amount}")
    int amount;

    @Override
    @Transactional
    public void handle(PayOsWebhookRequest request) {
        log.info("[system]: Received PayOS webhook for order code: {}", request.getData().getOrderCode());

        if (!PayOsSignatureUtils.verify(request, checkSumKey)) {
            log.warn("[system]: PayOS webhook verification failed for order code: {}. Invalid signature.", request.getData().getOrderCode());
            throw new BusinessException(ErrorCode.INVALID_SIGNATURE);
        }
        log.debug("[system]: PayOS webhook signature verified successfully for order code: {}.", request.getData().getOrderCode());

        PayOS payOS = payOSRepository.getPayOSByOrderCode(request.getData().getOrderCode())
                .orElseThrow(() -> {
                    log.warn("[system]: PayOS record not found for order code: {} from webhook.", request.getData().getOrderCode());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });
        log.debug("[system]: Found PayOS record for order code: {}. Current status: {}.", payOS.getOrderCode(), payOS.getStatus());

        if (amount != request.getData().getAmount()) {
            log.warn("[system]: Webhook amount mismatch for order code {}. Expected: {}, Received: {}.", payOS.getOrderCode(), amount, request.getData().getAmount());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        int updatedRows = payOSRepository
                .updatePayOsStatus(PaymentStatus.PAID,
                        PaymentType.UPGRADE_PREMIUM,
                        request.getData().getOrderCode(),
                        PaymentStatus.PENDING);
        if (updatedRows == 0) {
            log.info("[system]: PayOS record for order code {} was not in PENDING status or already updated. No further action taken.", payOS.getOrderCode());
            return;
        }
        log.info("[system]: PayOS record for order code {} successfully updated to PAID status.", payOS.getOrderCode());

        User user = userRepository.findById(payOSRepository.getUserIdByOrderCode(payOS.getOrderCode()))
                .orElseThrow(() -> {
                    log.error("[system]: User associated with PayOS order code {} not found.", payOS.getOrderCode());
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });
        log.debug("[system]: User {} associated with PayOS order code {} found. Current status: {}, Plan: {}.", user.getUsername(), payOS.getOrderCode(), user.getUserStatus(), user.getUserPlan());

        if (user.getUserStatus() == UserStatus.BANNED || user.getUserStatus() == UserStatus.DISABLED_BY_USER) {
            log.warn("[system]: User {} (ID: {}) associated with order code {} is banned or disabled. Cannot upgrade plan.", user.getUsername(), user.getId(), payOS.getOrderCode());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (user.getUserPlan() == UserPlan.PREMIUM) {
            log.info("[system]: User {} (ID: {}) is already a PREMIUM user. No plan upgrade needed for order code {}.", user.getUsername(), user.getId(), payOS.getOrderCode());
            return;
        }

        if (user.getUserStatus() == UserStatus.ACTIVE) {
            user.setUserPlan(UserPlan.PREMIUM);
            userRepository.save(user);
            log.info("[system]: User {} (ID: {}) plan successfully upgraded to PREMIUM for order code {}.", user.getUsername(), user.getId(), payOS.getOrderCode());
        } else {
            log.warn("[system]: User {} (ID: {}) is not in ACTIVE status (current status: {}). Plan not upgraded to PREMIUM for order code {}.", user.getUsername(), user.getId(), user.getUserStatus(), payOS.getOrderCode());
        }
    }
}
