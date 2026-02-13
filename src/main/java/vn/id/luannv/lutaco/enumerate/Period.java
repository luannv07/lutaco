package vn.id.luannv.lutaco.enumerate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Period {
    DAY,
    WEEK,
    MONTH,
    YEAR;

    public static Period from(String period) {
        try {
            return Period.valueOf(period.toUpperCase());
        } catch (Exception e) {
            log.error("Invalid period: {}", period);
            return null;
        }
    }
}
