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

    public static String normalizeMarkdown(String text) {
        if (text == null) return "";

        return text
                // bold
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")

                // italic
                .replaceAll("\\*(.*?)\\*", "$1")

                // heading
                .replaceAll("(?m)^#+\\s*", "")

                // multiple blank lines
                .replaceAll("\\n{3,}", "\n\n")

                .trim();
    }
}
