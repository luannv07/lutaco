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
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.PaymentStatus;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.PayOSRepository;
import vn.id.luannv.lutaco.repository.RoleRepository;
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
    RoleRepository roleRepository;

    @NonFinal
    @Value("${payment.check-sum-key}")
    String checkSumKey;

    @Override
    @Transactional
    public void handle(PayOsWebhookRequest request) {
        if (!PayOsSignatureUtils.verify(request, checkSumKey))
            throw new BusinessException(ErrorCode.INVALID_SIGNATURE);

        PayOS payOS = payOSRepository.getPayOSByOrderCode(request.getData().getOrderCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (payOS.getStatus() == PaymentStatus.PAID)
            return;

        User user = userRepository.findById(payOSRepository.getUserIdByOrderCode(payOS.getOrderCode()))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (user.getUserStatus() == UserStatus.BANNED || user.getUserStatus() == UserStatus.DISABLED_BY_USER)
            throw new BusinessException(ErrorCode.UNAUTHORIZED);

        if (user.getUserStatus() == UserStatus.ACTIVE) {
            Role premiumRole = roleRepository
                    .findByName(UserType.PREMIUM_USER.name())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            user.setRole(premiumRole);
        }

        userRepository.save(user);
        payOSRepository.save(payOS);
    }
}

