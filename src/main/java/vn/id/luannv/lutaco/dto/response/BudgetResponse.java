package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;

import java.time.LocalDate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    Long id;
    String userId;
    String username;
    String categoryId;
    String categoryName;
    String name;
    Long targetAmount;
    Period period;
    LocalDate startDate;
    LocalDate endDate;
    Float percentage;
    Long actualAmount;
    BudgetStatus status;
}
