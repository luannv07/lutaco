package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserUpdateRequest",
        description = "Request model for updating a user's profile information."
)
public class UserUpdateRequest {

    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    @Schema(
            description = "The user's full name.",
            example = "Johnathan Doe",
            minLength = 2,
            maxLength = 255
    )
    String fullName;

    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "The user's address.",
            example = "456 Oak Ave, Anytown, USA",
            maxLength = 255
    )
    String address;

    @Schema(
            description = "The user's gender.",
            example = "MALE",
            allowableValues = {"MALE", "FEMALE", "OTHER"}
    )
    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String gender;
}
