package vn.id.luannv.lutaco.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InsightDto {
    InsightLevel level;
    InsightCode code;
    Double value;
    String unit;
    String defaultColor;
    ColorTone colorTone;

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Getter
    public enum InsightLevel {
        WARN(ColorTone.WARNING, ColorTone.NEGATIVE_STRONG.hex),
        DANGER(ColorTone.NEGATIVE_STRONG, ColorTone.NEGATIVE_STRONG.hex),
        SUCCESS(ColorTone.POSITIVE_STRONG, ColorTone.NEGATIVE_STRONG.hex);

        ColorTone colorTone;
        String color;
    }

    @Getter
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor
    public enum ColorTone {
        POSITIVE_STRONG("#16a34a"),
        POSITIVE_SOFT("#86efac"),
        NEUTRAL("#94a3b8"),
        WARNING("#facc15"),
        NEGATIVE_SOFT("#fb7185"),
        NEGATIVE_STRONG("#e11d48");

        String hex;

    }

    public enum InsightCode {
        // ===== Expense =====
        EXPENSE_INCREASE,
        EXPENSE_DECREASE,

        // ===== Income =====
        INCOME_INCREASE,
        INCOME_DECREASE,

        // ===== Balance =====
        NEGATIVE_BALANCE,
        LOW_BALANCE,

        // ===== Category =====
        CATEGORY_OVER_SPENDING,
        CATEGORY_DOMINANT
    }
}
