package vn.id.luannv.lutaco.util;

import lombok.extern.slf4j.Slf4j;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.util.Optional;
import java.util.Map;

@Slf4j
public class EnumUtils {
    public static <T extends Enum<T>> T from(Class<T> enumClasses, Object value) {
        if (value == null || value.toString().isBlank()) {
            log.warn("[system]: Attempted to convert null or empty value to enum {}.", enumClasses.getName());
            throw new BusinessException(ErrorCode.ENUM_NOT_FOUND, Map.of("enum", value != null ? value : "null"));
        }

        return resolve(enumClasses, value)
                .orElseThrow(() -> {
                    log.error("[system]: Failed to convert value '{}' to enum type '{}'.", value, enumClasses.getName());
                    return new BusinessException(ErrorCode.ENUM_NOT_FOUND, Map.of("enum", value));
                });
    }

    public static <T extends Enum<T>> Optional<T> tryFrom(Class<T> enumClasses, Object value) {
        if (value == null || value.toString().isBlank()) {
            return Optional.empty();
        }

        return resolve(enumClasses, value);
    }

    private static <T extends Enum<T>> Optional<T> resolve(Class<T> enumClasses, Object value) {
        String normalized = value.toString().trim().toUpperCase();

        try {
            return Optional.of(Enum.valueOf(enumClasses, normalized));
        } catch (IllegalArgumentException ignored) {
            // Try alias-based lookup below.
        }

        if (EnumAliasMatcher.class.isAssignableFrom(enumClasses)) {
            for (T constant : enumClasses.getEnumConstants()) {
                if (((EnumAliasMatcher) constant).matches(normalized)) {
                    return Optional.of(constant);
                }
            }
        }

        return Optional.empty();
    }
}
