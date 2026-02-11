package vn.id.luannv.lutaco.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class TimeUtils {
    public static final LocalDateTime SAFE_MIN_DATE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    public static final LocalDateTime SAFE_MAX_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    // Mặc định nếu không tìm thấy timezone của user thì dùng cái này (Client chủ yếu ở VN)
    public static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";

    private static String format(LocalDateTime dateTime, String pattern) {
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

    /**
     * Convert từ UTC LocalDateTime sang String theo Timezone của User
     * Dùng cho việc hiển thị email, xuất file, sms...
     * * @param utcDateTime Thời gian gốc (đang lưu là UTC trong DB)
     * @param zoneId Timezone của user (VD: "Asia/Ho_Chi_Minh")
     * @param pattern Định dạng mong muốn (null sẽ lấy mặc định)
     * @return String thời gian đã convert
     */
    public static String formatToUserZone(LocalDateTime utcDateTime, String zoneId, String pattern) {
        if (utcDateTime == null) return null;

        // Validate inputs
        if (pattern == null) pattern = "dd/MM/yyyy HH:mm:ss";
        if (zoneId == null || zoneId.trim().isEmpty()) zoneId = DEFAULT_TIMEZONE;

        try {
            // Bước 1: Định nghĩa rằng cái utcDateTime này ĐANG LÀ UTC
            ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));

            // Bước 2: Tìm múi giờ đích
            ZoneId userZone = ZoneId.of(zoneId);

            // Bước 3: Dịch chuyển thời gian (Shift time)
            ZonedDateTime userZoned = utcZoned.withZoneSameInstant(userZone);

            // Bước 4: Format
            return userZoned.format(DateTimeFormatter.ofPattern(pattern));

        } catch (Exception e) {
            // Trường hợp user nhập timezone bậy bạ (VD: "Mars/Alien")
            // Fallback về mặc định hiển thị giờ gốc hoặc log error
            log.info("Error converting timezone: {}", e.getMessage());
            return format(utcDateTime, pattern);
        }
    }
}
