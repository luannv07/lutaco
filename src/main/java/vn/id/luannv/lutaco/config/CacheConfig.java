package vn.id.luannv.lutaco.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.id.luannv.lutaco.domain.otp.OtpInfo;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.default.expire-after-write-minutes}")
    private int defaultExpireAfterWriteMinutes;

    @Value("${app.cache.default.maximum-size}")
    private int defaultMaximumSize;

    @Value("${app.cache.rate-limit.expire-after-access-minutes}")
    private int rateLimitExpireAfterWriteMinutes;

    @Value("${app.cache.rate-limit.maximum-size}")
    private int rateLimitMaximumSize;

    @Value("${app.cache.otp.expire-after-write-minutes}")
    private int otpExpireAfterWriteMinutes;

    @Value("${app.cache.otp.maximum-size}")
    private int otpMaximumSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(defaultExpireAfterWriteMinutes, TimeUnit.MINUTES)
                        .maximumSize(defaultMaximumSize)
        );
        return cacheManager;
    }

    @Bean
    public Cache<String, RateLimitingFilter.RequestInfo> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(rateLimitExpireAfterWriteMinutes, TimeUnit.MINUTES)
                .maximumSize(rateLimitMaximumSize)
                .build();
    }

    @Bean
    public Cache<String, OtpInfo> otpCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(otpExpireAfterWriteMinutes, TimeUnit.MINUTES)
                .maximumSize(otpMaximumSize)
                .build();
    }
}
