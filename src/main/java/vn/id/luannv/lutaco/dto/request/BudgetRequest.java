package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "BudgetRequest", description = "Request for creating or updating a budget")
public class BudgetRequest {

    @Schema(description = "ID of the category", example = "CAT_123")
    String categoryId;

    @Schema(description = "Name of the budget", example = "Monthly Food Budget")
    String name;

    @Schema(description = "Target amount for the budget", example = "5000000")
    Long targetAmount;

    @Schema(description = "Period of the budget", example = "MONTHLY")
    String period;

    @Schema(description = "Start date of the budget", example = "2024-01-01")
    LocalDate startDate;
}
