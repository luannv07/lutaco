package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum PeriodRange {
    LAST_7_DAYS("config.enum.period.range.last_7_days"),
    LAST_1_MONTH("config.enum.period.range.last_1_month"),
    LAST_3_MONTHS("config.enum.period.range.last_3_months"),
    LAST_1_YEAR("config.enum.period.range.last_1_year"),
    ALL_TIME("config.enum.period.range.all_time")
    ;
    String display;
}
