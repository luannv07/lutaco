package vn.id.luannv.lutaco.enumerate;

import vn.id.luannv.lutaco.util.EnumAliasMatcher;

import java.time.LocalDate;
import java.util.Set;

public enum Period implements EnumAliasMatcher {
    DAY("DAY", "DAILY") {
        @Override
        public LocalDate addTo(LocalDate date) {
            return date.plusDays(1);
        }
    },
    WEEK("WEEK", "WEEKLY") {
        @Override
        public LocalDate addTo(LocalDate date) {
            return date.plusWeeks(1);
        }
    },
    MONTH("MONTH", "MONTHLY") {
        @Override
        public LocalDate addTo(LocalDate date) {
            return date.plusMonths(1);
        }
    },
    YEAR("YEAR", "YEARLY") {
        @Override
        public LocalDate addTo(LocalDate date) {
            return date.plusYears(1);
        }
    };

    private final Set<String> aliases;

    Period(String... aliases) {
        this.aliases = Set.of(aliases);
    }

    public abstract LocalDate addTo(LocalDate date);

    public LocalDate calculateNextDate(LocalDate date) {
        return addTo(date);
    }

    public LocalDate calculateEndDate(LocalDate startDate) {
        return addTo(startDate).minusDays(1);
    }

    @Override
    public boolean matches(String normalizedValue) {
        return aliases.contains(normalizedValue);
    }
}
