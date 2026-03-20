package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum FrequentType {
    DAILY("config.enum.frequent.type.daily") {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusDays(1);
        }
    },
    WEEKLY("config.enum.frequent.type.weekly") {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusWeeks(1);
        }
    },
    MONTHLY("config.enum.frequent.type.monthly") {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusMonths(1);
        }
    },
    YEARLY("config.enum.frequent.type.yearly") {
        @Override
        public LocalDate calculateNextDate(LocalDate date) {
            return date.plusYears(1);
        }
    };

    String display;

    public abstract LocalDate calculateNextDate(LocalDate date);
}
