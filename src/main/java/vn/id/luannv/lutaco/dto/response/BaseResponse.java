package vn.id.luannv.lutaco.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    Boolean success;
    ErrorCode errorCode;
    Map<String, Object> params;
    String messageKey;
    T data;
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Trả về một success body
     * @param data Dữ liệu (nếu có)
     * @param messageKey Message dạng key để Frontend áp đa ngôn ngữ
     * @return BaseResponse<T> Trả về với data T
     * @param <T> Dữ liệu chỉ định
     */
    public static <T> BaseResponse<T> success(T data, String messageKey) {
        return  BaseResponse.<T>builder()
                .success(true)
                .data(data)
                .messageKey(messageKey)
                .build();
    }

    /**
     * Trả về một error body
     * @param params Các cặp dạng field : value lỗi
     * @param errorCode Mã lỗi (tên Enum ErrorCode)
     * @return BaseResponse Chuẩn class trả về
     * @param <Void> Không có data, mặc định Void
     */
    public static <Void> BaseResponse<Void> error(Map<String, Object> params, ErrorCode errorCode) {
        return BaseResponse.<Void>builder()
                .success(false)
                .messageKey(errorCode.getMessage())
                .errorCode(errorCode)
                .params(params)
                .build();
    }
}
