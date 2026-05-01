package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionRequest {

    @NotBlank(message = "{validation.required}")
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryId;

    @NotNull(message = "{validation.required}")
    @Positive(message = "{validation.field.positive}")
    Long amount;

    @NotNull(message = "{validation.required}")
    LocalDateTime transactionDate;

    @NotBlank(message = "{validation.required}")
    @Length(max = 255, message = "{validation.field.too_long}")
    String walletId;

    @Size(max = 500, message = "{validation.field.too_long}")
    @Length(max = 255, message = "{validation.field.too_long}")
    String note;
}
