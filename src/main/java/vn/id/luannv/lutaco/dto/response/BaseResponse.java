package vn.id.luannv.lutaco.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a standardized API response wrapper.
 * This class is used to provide a consistent structure for all API responses,
 * indicating success or failure and including relevant data, error codes, and messages.
 *
 * @param <T> the type of the data payload included in the response.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private Boolean success;
    private String errorCode;
    private String messageKey;
    private String message;
    private Map<String, Object> params;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Creates a standardized success response with a data payload.
     *
     * @param data    The data payload to be returned.
     * @param message The resolved, human-readable success message.
     * @param <T>     The type of the data payload.
     * @return A {@code BaseResponse} instance representing a successful outcome.
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a standardized success response without a data payload.
     *
     * @param message The resolved, human-readable success message.
     * @return A {@code BaseResponse} instance representing a successful outcome.
     */
    public static BaseResponse<Void> success(String message) {
        return BaseResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Creates a standardized error response.
     *
     * @param errorCode The error code enum that represents the error.
     * @param message   The resolved, human-readable error message.
     * @param params    A map of additional parameters related to the error (e.g., validation details).
     * @return A {@code BaseResponse} instance representing a failed outcome.
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, Map<String, Object> params) {
        return BaseResponse.<T>builder()
                .success(false)
                .errorCode(errorCode.name())
                .messageKey(errorCode.getMessage())
                .message(message)
                .params(params)
                .build();
    }
}
