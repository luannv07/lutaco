package vn.id.luannv.lutaco.util;

import java.util.Locale;

public final class StringUtils {

    private StringUtils() {
        // Utility class
    }

    public static String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
