package vn.id.luannv.lutaco.util;

import lombok.extern.slf4j.Slf4j;
import vn.id.luannv.lutaco.dto.request.PayOSRequest;
import vn.id.luannv.lutaco.dto.request.PayOsWebhookData;
import vn.id.luannv.lutaco.dto.request.PayOsWebhookRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class PayOsSignatureUtils {
    public static boolean verify(PayOsWebhookRequest req, String checksumKey) {
        try {
            PayOsWebhookData d = req.getData();

            // 1. map toàn bộ field cần verify
            Map<String, String> data = new HashMap<>();
            data.put("orderCode", String.valueOf(d.getOrderCode()));
            data.put("amount", String.valueOf(d.getAmount()));
            data.put("description", d.getDescription());
            data.put("accountNumber", d.getAccountNumber());
            data.put("reference", d.getReference());
            data.put("transactionDateTime", d.getTransactionDateTime());
            data.put("currency", d.getCurrency());
            data.put("paymentLinkId", d.getPaymentLinkId());
            data.put("code", d.getCode());
            data.put("desc", d.getDesc());
            data.put("counterAccountBankId", d.getCounterAccountBankId());
            data.put("counterAccountBankName", d.getCounterAccountBankName());
            data.put("counterAccountName", d.getCounterAccountName());
            data.put("counterAccountNumber", d.getCounterAccountNumber());
            data.put("virtualAccountName", d.getVirtualAccountName());
            data.put("virtualAccountNumber", d.getVirtualAccountNumber());

            List<String> keys = new ArrayList<>(data.keySet());
            Collections.sort(keys);

            StringBuilder raw = new StringBuilder();
            for (String key : keys) {
                String value = data.get(key);
                if (value == null) value = "";

                raw.append(key)
                        .append("=")
                        .append(value)
                        .append("&");
            }

            raw.deleteCharAt(raw.length() - 1);

            String expectedSignature = hmacSha256(raw.toString(), checksumKey);
            log.debug("Generated signature for webhook: {}", expectedSignature);

            return expectedSignature.equals(req.getSignature());

        } catch (Exception e) {
            log.error("Failed to verify PayOS webhook signature. Error: {}", e.getMessage(), e);
            return false;
        }
    }

    public static String generateHmacSha256ForCreatePaymentRequest(PayOSRequest request, String checksumKey) {
        String rawData = String.format(
                "amount=%s&cancelUrl=%s&description=%s&orderCode=%s&returnUrl=%s",
                request.getAmount(),
                request.getCancelUrl(),
                request.getDescription(),
                request.getOrderCode(),
                request.getReturnUrl()
        );
        log.debug("Raw data for PayOS payment request signature generation: {}", rawData);
        return hmacSha256(rawData, checksumKey);
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            log.error("HMAC SHA256 signature generation failed. Error: {}", e.getMessage(), e);
            throw new RuntimeException("SIGNATURE_VERIFY_FAILED", e);
        }
    }
}
