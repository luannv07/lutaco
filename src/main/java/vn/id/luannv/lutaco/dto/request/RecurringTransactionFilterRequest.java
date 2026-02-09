package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.FrequentType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionFilterRequest extends BaseFilterRequest {
    FrequentType frequentType;
}
