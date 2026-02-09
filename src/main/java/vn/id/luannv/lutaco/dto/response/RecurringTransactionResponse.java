package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.FrequentType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionResponse {

    Long id;

    String transactionId;

    LocalDate startDate;

    LocalDate nextDate;

    FrequentType frequentType;
}
