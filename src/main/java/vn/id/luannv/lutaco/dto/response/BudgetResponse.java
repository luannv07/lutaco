package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

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
    String period;
    LocalDate startDate;
    LocalDate endDate;
    Float percentage;
    Long actualAmount;
}
