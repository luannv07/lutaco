package vn.id.luannv.lutaco.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.id.luannv.lutaco.dto.request.BaseFilterRequest;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserAuditFilterRequest extends BaseFilterRequest {
    private String username;
    private String userAgent;
    private String clientIp;
    private String requestUri;
    private Long executionTimeMsFrom;
    private Long executionTimeMsTo;
    private String paramContent;
    private LocalDate createdDateFrom;
    private LocalDate createdDateTo;
}
