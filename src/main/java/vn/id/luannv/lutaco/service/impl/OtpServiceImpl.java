package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.service.OtpService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void sendOtp(String email, OtpType otpType, String username) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void verifyOtp(VerifyOtpRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
