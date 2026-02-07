package vn.id.luannv.lutaco.util;

import vn.id.luannv.lutaco.dto.PeriodWindow;
import vn.id.luannv.lutaco.enumerate.PeriodRange;

import java.time.LocalDateTime;

import static vn.id.luannv.lutaco.util.DateTimeUtils.SAFE_MIN_DATE;

public class PeriodWindowFactory {

    public static PeriodWindow of(PeriodRange range) {
        LocalDateTime now = LocalDateTime.now();

        return switch (range) {
            case LAST_7_DAYS -> {
                LocalDateTime from = now.minusDays(7);
                yield new PeriodWindow(
                        from, now,
                        from.minusDays(7), from
                );
            }

            case THIS_MONTH -> {
                LocalDateTime from = now.withDayOfMonth(1);
                yield new PeriodWindow(
                        from, now,
                        from.minusMonths(1),
                        from.minusSeconds(1)
                );
            }

            case LAST_MONTH -> {
                LocalDateTime startThisMonth = now.withDayOfMonth(1);
                LocalDateTime from = startThisMonth.minusMonths(1);
                LocalDateTime to = startThisMonth.minusSeconds(1);
                yield new PeriodWindow(
                        from, to,
                        from.minusMonths(1),
                        from.minusSeconds(1)
                );
            }

            case LAST_QUARTER -> {
                LocalDateTime from = now.minusMonths(3);
                yield new PeriodWindow(
                        from, now,
                        from.minusMonths(3),
                        from
                );
            }

            case ALL_TIME -> new PeriodWindow(
                    SAFE_MIN_DATE,
                    now,
                    SAFE_MIN_DATE,
                    SAFE_MIN_DATE
            );
        };
    }
}
