package vn.id.luannv.lutaco.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BudgetFilterRequest extends BaseFilterRequest {

        String period;

        String name;
}
