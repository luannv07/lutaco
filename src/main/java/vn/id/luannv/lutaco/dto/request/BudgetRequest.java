package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetRequest {
    String categoryId;
    String name;
    Long targetAmount;
    String period;
    LocalDate startDate;
}
