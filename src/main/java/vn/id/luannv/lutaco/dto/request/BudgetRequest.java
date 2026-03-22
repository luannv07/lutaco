package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.dto.EnumDisplay;
import vn.id.luannv.lutaco.enumerate.Period;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "BudgetRequest", description = "Request for creating or updating a budget")
public class BudgetRequest {

    @Schema(description = "ID of the category", example = "9c2f8b7d-3a0d-44c2-98d6-efb1e4bfc321")
    String categoryId;

    @Schema(description = "Name of the budget", example = "Monthly Coffee Budget")
    String name;

    @Schema(description = "Target amount for the budget", example = "5000000")
    Long targetAmount;

    @Schema(description = "Period of the budget", example = "MONTH")
    String period;

    @Schema(description = "Start date of the budget", example = "2024-01-01")
    LocalDate startDate;
}
