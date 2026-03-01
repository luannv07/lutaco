package vn.id.luannv.lutaco.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.caffeine.expire-after-write-minutes}")
    private int expireAfterWriteMinutes;

    @Value("${app.cache.caffeine.maximum-size}")
    private int maximumSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                        .maximumSize(maximumSize)
        );
        return cacheManager;
    }

    @Bean
    public Cache<String, RateLimitingFilter.RequestInfo> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(expireAfterWriteMinutes, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .build();
    }
}
