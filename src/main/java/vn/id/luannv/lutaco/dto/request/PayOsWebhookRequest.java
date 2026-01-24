package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOsWebhookRequest {
    String code;
    String desc;
    Boolean success;
    PayOsWebhookData data;
    String signature;
}

