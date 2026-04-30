package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserFilterRequest extends BaseFilterRequest {

        @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String username;

    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
        String address;

        @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String userStatus;

    @Range(min = 0, max = 255, message = "{validation.field.size_not_in_range}")
        Integer roleId;

        @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String userPlan;
}
