package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOsWebhookData {
    String accountNumber;
    Integer amount;
    String description;
    String reference;
    String transactionDateTime;

    String virtualAccountNumber;

    String counterAccountBankId;
    String counterAccountBankName;
    String counterAccountName;
    String counterAccountNumber;

    String virtualAccountName;
    String currency;

    Integer orderCode;
    String paymentLinkId;

    String code;
    String desc;
}
