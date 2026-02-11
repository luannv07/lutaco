package vn.id.luannv.lutaco.util;

import vn.id.luannv.lutaco.dto.PeriodWindow;
import vn.id.luannv.lutaco.enumerate.PeriodRange;

import java.time.LocalDateTime;

import static vn.id.luannv.lutaco.util.TimeUtils.SAFE_MIN_DATE;

public class PeriodWindowFactory {

    public static PeriodWindow of(PeriodRange range) {
        LocalDateTime now = LocalDateTime.now();

        return switch (range) {

            case LAST_7_DAYS -> buildRolling(now, 7, TimeUnit.DAYS);

            case LAST_1_MONTH -> buildRolling(now, 1, TimeUnit.MONTHS);

            case LAST_3_MONTHS -> buildRolling(now, 3, TimeUnit.MONTHS);

            case LAST_1_YEAR -> buildRolling(now, 1, TimeUnit.YEARS);

            case ALL_TIME -> new PeriodWindow(
                    SAFE_MIN_DATE,
                    now,
                    SAFE_MIN_DATE,
                    SAFE_MIN_DATE
            );
        };
    }

    private static PeriodWindow buildRolling(
            LocalDateTime now,
            long amount,
            TimeUnit unit
    ) {
        LocalDateTime from = minus(now, amount, unit);
        LocalDateTime prevFrom = minus(from, amount, unit);

        return new PeriodWindow(
                from,
                now,
                prevFrom,
                from
        );
    }

    private static LocalDateTime minus(
            LocalDateTime time,
            long amount,
            TimeUnit unit
    ) {
        return switch (unit) {
            case DAYS -> time.minusDays(amount);
            case MONTHS -> time.minusMonths(amount);
            case YEARS -> time.minusYears(amount);
        };
    }

    private enum TimeUnit {
        DAYS, MONTHS, YEARS
    }
}