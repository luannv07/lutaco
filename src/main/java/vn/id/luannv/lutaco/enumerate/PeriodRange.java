package vn.id.luannv.lutaco.enumerate;

import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.util.Map;

public enum PeriodRange {
    LAST_7_DAYS,
    THIS_MONTH,
    LAST_MONTH,
    LAST_QUARTER,
    ALL_TIME;

    public static PeriodRange from(String range) {
        return switch (range) {
            case "LAST_7_DAYS" -> LAST_7_DAYS;
            case "THIS_MONTH" -> THIS_MONTH;
            case "LAST_MONTH" -> LAST_MONTH;
            case "LAST_QUARTER" -> LAST_QUARTER;
            case "ALL_TIME" -> ALL_TIME;
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMS, Map.of("range", range));
        };
    }
}
