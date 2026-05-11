package vn.id.luannv.lutaco.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiExtractResponse {

    private TransactionDraft transactionDraft;
    private Bill bill;
    private String rawText;
    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionDraft {
        private Long amount;
        private String transactionDate;
        private String note;
        private String suggestedCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bill {
        private String storeName;
        private String storeAddress;
        private String date;
        private String time;
        private List<BillItem> items;
        private Long subtotal;
        private Long discount;
        private Long tax;
        private Long total;
        private String currency;
        private String paymentMethod;
        private String category;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BillItem {
        private String name;
        private Integer quantity;
        private Long unitPrice;
        private Long totalPrice;
        private String note;
    }
}


