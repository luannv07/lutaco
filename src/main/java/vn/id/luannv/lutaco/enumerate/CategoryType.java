package vn.id.luannv.lutaco.enumerate;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public enum CategoryType {
    INCOME,
    EXPENSE;

    public static boolean isValidCategoryType(String categoryType) {
        return Arrays.stream(CategoryType.values()).anyMatch(cateType -> cateType.name().equals(categoryType));
    }

    public static CategoryType from(Object obj) {
        if (obj == null) return null;
        try {
            return CategoryType.valueOf(obj.toString());
        } catch (Exception e) {
            log.info("Invalid category type: {}", obj);
            return null;
        }
    }
}
