package vn.id.luannv.lutaco.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetResponse {

    Long id;
    String name;
    Long categoryId;
    String categoryName;
    Period period;
    BigDecimal targetAmount;
    BigDecimal actualAmount;
    BigDecimal remainingAmount;
    BigDecimal percentage;
    BudgetStatus status;
    LocalDate startDate;
    LocalDate endDate;
    String userId;
}
