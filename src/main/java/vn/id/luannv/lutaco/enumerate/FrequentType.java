package vn.id.luannv.lutaco.enumerate;

import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.time.LocalDate;
import java.util.Map;

public enum FrequentType {
    DAILY {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusDays(1);
        }
    },
    WEEKLY {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusWeeks(1);
        }
    },
    MONTHLY {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusMonths(1);
        }
    },
    YEARLY {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusYears(1);
        }
    };

    public abstract LocalDate calculateNextDate(LocalDate date);

    public static FrequentType from(String value) {
        try {
            return FrequentType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_PARAMS,
                    Map.of("frequentType", value)
            );
        }
    }
}
