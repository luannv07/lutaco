package vn.id.luannv.lutaco.enumerate;

import java.time.LocalDate;

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
}
