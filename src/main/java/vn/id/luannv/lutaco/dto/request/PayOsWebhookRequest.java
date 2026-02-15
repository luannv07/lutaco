package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "PayOsWebhookRequest", description = "Request from PayOS webhook")
public class PayOsWebhookRequest {

    @Schema(description = "Response code from PayOS", example = "00")
    String code;

    @Schema(description = "Description of the response", example = "Success")
    String desc;

    @Schema(description = "Indicates if the transaction was successful", example = "true")
    Boolean success;

    @Schema(description = "Webhook data")
    PayOsWebhookData data;

    @Schema(description = "Signature to verify the request", example = "a1b2c3d4e5f6")
    String signature;
}
