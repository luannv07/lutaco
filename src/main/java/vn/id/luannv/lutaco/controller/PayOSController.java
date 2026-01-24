package vn.id.luannv.lutaco.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.service.PayOsService;

@Slf4j
@RestController
@RequestMapping(("/api/v1/payment"))
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSController {
    PayOsService payOsService;

    @PostMapping("/premium-user")
    public ResponseEntity<BaseResponse<PayOSResponse>> createPayment() {
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success(
                                payOsService.createPayment(PaymentType.UPGRADE_PREMIUM),
                                MessageKeyConst.Success.CREATED
                        )
                );
    }
}
