package vn.id.luannv.lutaco.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalizationUtils {
    MessageSource messageSource;
    LocaleResolver localeResolver;

    public String getLocalizedMessage(String key, Object... args) {
        try {
            HttpServletRequest request =((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            Locale locale = localeResolver.resolveLocale(request);
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            return key;
        }
    }
}
