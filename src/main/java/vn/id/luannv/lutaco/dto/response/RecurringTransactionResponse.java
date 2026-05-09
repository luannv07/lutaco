package vn.id.luannv.lutaco.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.enumerate.FrequentType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionResponse {

    Long id;
    Long categoryId;
    String categoryName;
    CategoryType categoryType;
    Long walletId;
    String walletName;
    Long amount;
    String note;
    FrequentType frequentType;
    LocalDate startDate;
    LocalDate nextDate;
    LocalDate endDate;
    Boolean activeFlg;
}
