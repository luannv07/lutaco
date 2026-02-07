package vn.id.luannv.lutaco.insight;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;

import java.util.List;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InsightContext {
    Long incomeThisMonth;
    Long incomeLastMonth;
    Long expenseThisMonth;
    Long expenseLastMonth;
    Long balance;
    List<CategoryExpenseResponse> categories;
}
