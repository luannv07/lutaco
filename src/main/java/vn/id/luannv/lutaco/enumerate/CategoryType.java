package vn.id.luannv.lutaco.enumerate;

import java.util.Arrays;

public enum CategoryType {
    INCOME,
    EXPENSE;

    public static boolean isValidCategoryType(String categoryType) {
        return Arrays.stream(CategoryType.values()).anyMatch(cateType -> cateType.name().equals(categoryType));
    }
}
