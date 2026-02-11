package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionFilterRequest extends BaseFilterRequest {
    @Length(max = 255, message = "{validation.field.too_long}")
    String frequentType;
}
