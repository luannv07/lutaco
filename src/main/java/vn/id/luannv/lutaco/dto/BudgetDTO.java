package vn.id.luannv.lutaco.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetDTO {
    Long id;
    String name;
    String categoryId;
    String categoryName;
    String username;
    Long targetAmount;
    Long actualAmount;
    Float percentage;
    Period period;
    LocalDateTime startDate;
    LocalDateTime endDate;
    BudgetStatus status;
}
