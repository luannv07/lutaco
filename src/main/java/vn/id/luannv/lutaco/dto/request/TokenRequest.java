package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenRequest {
    @NotBlank(message = "{input.required}")
    @Length(max = 255, message = "{input.invalid}")
    @Schema(defaultValue = "{{bearer_token}}")
    String token;
}
