package vn.id.luannv.lutaco.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class BusinessException extends RuntimeException {
    ErrorCode errorCode;
    Map<String, Object> params;

    /**
     * Constructs a new business exception with the specified error code.
     *
     * @param errorCode the error code
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.params = null;
    }

    /**
     * Constructs a new business exception with the specified error code and parameters.
     *
     * @param errorCode the error code
     * @param params    the parameters related to the error
     */
    public BusinessException(ErrorCode errorCode, Map<String, Object> params) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.params = params;
    }
}
