package vn.id.luannv.lutaco.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeFormatUtils {
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null)
            return null;
        if (pattern == null)
            pattern = "dd/MM/yyyy HH:mm:ss";
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
