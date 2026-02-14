package vn.id.luannv.lutaco.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomizeNumberUtils {
    public static BigDecimal formatDecimal(Number value, int scale) {
        if (value == null) return BigDecimal.ZERO;

        return BigDecimal.valueOf(value.doubleValue())
                .setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros();
    }
    public static Float percentage(Number value, Number total) {
        if (value == null || total == null) return null;

        float t = total.floatValue();
        if (t == 0) return 0f;

        return (value.floatValue() / t) * 100f;
    }
}
