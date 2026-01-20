package vn.id.luannv.lutaco.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
@Setter

public class BusinessException extends RuntimeException{
    ErrorCode errorCode;
    Map<String, Object> params;

    /**
     * Constructor tuỳ biến với message mặc định là ErrorCode.messageKey()
     * @param errorCode Mã lỗi (ErrorCode.name())
     * @param params Cặp field : value lỗi
     */
    public BusinessException(ErrorCode errorCode, Map<String, Object> params) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.params = params;
    }

    /**
     * Constructor chỉ có ErrorCode
     * @param errorCode Mã lỗi (ErrorCode.name())
     */
    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null);
    }
}
