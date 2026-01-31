package vn.id.luannv.lutaco.event;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.event.entity.UserRegistered;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.service.WalletService;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserRegisteredEvent {
    OtpService otpService;
    WalletService walletService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(UserRegistered event) {
        walletService.createDefaultWallet(event.id());
        otpService.sendOtp(event.email(), OtpType.REGISTER);
    }
}
