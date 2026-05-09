package vn.id.luannv.lutaco.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetFilterRequest extends BaseFilterRequest {

    String period;

    String name;

    String status;

    Long categoryId;
}
