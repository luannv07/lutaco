package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
/*
 * Không cần validate request vì được build từ phía PayOS gửi về webhook
 */
public class PayOsWebhookRequest {
    String code;
    String desc;
    Boolean success;
    PayOsWebhookData data;
    String signature;
}

