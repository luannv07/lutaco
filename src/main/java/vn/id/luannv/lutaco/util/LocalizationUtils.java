package vn.id.luannv.lutaco.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import vn.id.luannv.lutaco.service.I18nCacheService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalizationUtils {
    LocaleResolver localeResolver;
    I18nCacheService i18nCacheService;

    public String getLocalizedMessage(String key, Object... args) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes()).getRequest();

            Locale locale = localeResolver.resolveLocale(request);

            return i18nCacheService.getMessage(key, locale, args);
        } catch (Exception e) {
            return key;
        }
    }

    public String getCurrentLocaleKey() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes()).getRequest();
            return localeResolver.resolveLocale(request).toLanguageTag(); // "vi", "en", ...
        } catch (Exception e) {
            return "vi"; // fallback
        }
    }
}
