package vn.id.luannv.lutaco.enumerate;

import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.util.Map;

public enum FrequentType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

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
