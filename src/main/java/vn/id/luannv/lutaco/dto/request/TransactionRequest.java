package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionRequest {

    @NotBlank(message = "{input.required}")
    String categoryId;

    @NotNull(message = "{input.required}")
    @Positive(message = "{input.invalid}")
    Long amount;

    @NotNull(message = "{input.required}")
    String transactionType;

    @NotNull(message = "{input.required}")
    LocalDateTime transactionDate;

    String note;
}
