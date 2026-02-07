package vn.id.luannv.lutaco.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    public static final LocalDateTime SAFE_MIN_DATE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    public static final LocalDateTime SAFE_MAX_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null)
            return null;
        if (pattern == null)
            pattern = "dd/MM/yyyy HH:mm:ss";
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime convertSafeDate(LocalDateTime dateTime, boolean isMax) {
        if (isMax)
            return dateTime == null || dateTime.isAfter(SAFE_MAX_DATE) ? SAFE_MAX_DATE : dateTime;
        return dateTime == null || dateTime.isBefore(SAFE_MIN_DATE) ? SAFE_MIN_DATE : dateTime;
    }
}
