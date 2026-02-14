package vn.id.luannv.lutaco.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumUtils {
    public static <T extends Enum<T>> T from(Class<T> enumClasses, Object value) {
        if (value == null || value.toString().isEmpty()) {
            return null;
        }

        try {
            return Enum.valueOf(enumClasses, value.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("error when convert {} to enum {}", value, enumClasses.getName());
            return null;
        }
    }
}
