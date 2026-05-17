package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {

    Long id;

    Long categoryId;

    String categoryName;

    CategoryType categoryTypeCd;

    Long amount;

    Instant transactionDate;

    String note;

    Instant createdDate;

    Long walletId;

    String walletName;
}
