package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum CategoryType {
    INCOME("config.enum.category.type.income"),
    EXPENSE("config.enum.category.type.expense");
    String display;
}
