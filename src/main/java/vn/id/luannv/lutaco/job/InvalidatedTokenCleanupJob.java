package vn.id.luannv.lutaco.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvalidatedTokenCleanupJob {
    InvalidatedTokenService invalidatedTokenService;

    @Scheduled(cron = "0 15 */1 * * ?") // mỗi giờ, phút 15
    @Async
    public void cleanupExpiredTokens() {
        log.info("⏰ Start cleaning expired invalidated tokens");
        invalidatedTokenService.deleteExpiredTokens();
        log.info("✅ Finished cleaning expired invalidated tokens");
    }
}
