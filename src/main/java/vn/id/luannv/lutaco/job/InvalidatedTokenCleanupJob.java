package vn.id.luannv.lutaco.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvalidatedTokenCleanupJob {
    InvalidatedTokenService invalidatedTokenService;

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void cleanupExpiredTokens() {
        log.info("⏰ Start cleaning expired invalidated tokens");
        invalidatedTokenService.deleteExpiredTokens();
        log.info("✅ Finished cleaning expired invalidated tokens");
    }
}
