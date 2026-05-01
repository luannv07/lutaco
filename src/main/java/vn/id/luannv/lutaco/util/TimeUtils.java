package vn.id.luannv.lutaco.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class TimeUtils {
    public static final LocalDateTime SAFE_MIN_DATE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    public static final LocalDateTime SAFE_MAX_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    public static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";
    public static final String DEFAULT_PATTERN = "dd/MM/yyyy HH:mm:ss";

    private static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null)
            return null;
        if (pattern == null)
            pattern = DEFAULT_PATTERN;
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime convertSafeDate(LocalDateTime dateTime, boolean isMax) {
        if (isMax)
            return dateTime == null || dateTime.isAfter(SAFE_MAX_DATE) ? SAFE_MAX_DATE : dateTime;
        return dateTime == null || dateTime.isBefore(SAFE_MIN_DATE) ? SAFE_MIN_DATE : dateTime;
    }

    /**
     * Convert {@link Instant} (UTC) sang String theo timezone của user.
     *
     * <p>Đây là method core, toàn bộ overload khác sẽ delegate về đây.</p>
     *
     * @param instant Thời gian UTC (chuẩn nên lưu trong DB)
     * @param zoneId  Timezone đích (VD: "Asia/Ho_Chi_Minh"), nếu null sẽ dùng mặc định
     * @param pattern Format output (VD: "dd/MM/yyyy HH:mm:ss"), nếu null sẽ dùng mặc định
     * @return String đã format theo timezone user, hoặc null nếu input null
     */
    public static String formatToUserZone(Instant instant, String zoneId, String pattern) {
        if (instant == null) return null;

        if (pattern == null || pattern.isBlank()) {
            pattern = DEFAULT_PATTERN;
        }

        if (zoneId == null || zoneId.isBlank()) {
            zoneId = DEFAULT_TIMEZONE;
        }

        try {
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(zoneId));
            return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.warn("[TimeUtils] Failed to format instant '{}' to zone '{}' with pattern '{}'. Fallback to ISO. Error: {}",
                    instant, zoneId, pattern, e.getMessage());
            return instant.toString(); // ISO-8601 fallback
        }
    }

    public static Date toDate(Instant instant) {
        if (instant == null) return new Date();
        return Date.from(instant);
    }

    public static Instant toInstant(Date date) {
        if (date == null) return Instant.now();
        return date.toInstant();
    }
}
