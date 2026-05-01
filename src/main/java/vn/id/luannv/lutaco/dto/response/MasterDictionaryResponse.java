package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterDictionaryResponse {

    Long id;
    String dictGroup;
    String code;
    String valueVi;
    String valueEn;
    Boolean activeFlg;
    Integer displayOrder;
    String createdBy;
    Instant createdDate;
    String updatedBy;
    Instant updatedDate;
}
