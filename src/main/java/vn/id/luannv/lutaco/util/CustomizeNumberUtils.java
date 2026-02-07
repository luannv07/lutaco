package vn.id.luannv.lutaco.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomizeNumberUtils {
    public static BigDecimal formatDecimal(Number value, int scale) {
        if (value == null) return BigDecimal.ZERO;

        return BigDecimal.valueOf(value.doubleValue())
                .setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros();
    }
}
