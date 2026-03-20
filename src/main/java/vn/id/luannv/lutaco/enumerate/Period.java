package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum Period {
    DAY("config.enum.period.day"),
    WEEK("config.enum.period.week"),
    MONTH("config.enum.period.month"),
    YEAR("config.enum.period.year")
    ;
    String display;
}
