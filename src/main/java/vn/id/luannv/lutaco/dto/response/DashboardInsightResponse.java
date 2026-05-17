package vn.id.luannv.lutaco.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardInsightResponse {
    InsightLevel levelCd;
    InsightCode codeCd;
    Double value;
    String unit;
    String color;

    public enum InsightLevel {
        SUCCESS,
        WARN,
        DANGER
    }

    public enum InsightCode {
        EXPENSE_INCREASE,
        EXPENSE_DECREASE,
        INCOME_INCREASE,
        INCOME_DECREASE,
        NEGATIVE_BALANCE,
        CATEGORY_DOMINANT
    }
}

