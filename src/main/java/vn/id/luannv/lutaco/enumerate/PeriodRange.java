package vn.id.luannv.lutaco.enumerate;

import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.util.Map;

public enum PeriodRange {
    LAST_7_DAYS,
    LAST_1_MONTH,
    LAST_3_MONTHS,
    LAST_1_YEAR,
    ALL_TIME
}