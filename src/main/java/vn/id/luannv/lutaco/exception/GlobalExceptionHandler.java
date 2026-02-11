package vn.id.luannv.lutaco.exception;

import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LocalizationUtils localizationUtils;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business error: {} - Params: {}", ex.getErrorCode(), ex.getParams());
        String message = localizationUtils.getLocalizedMessage(ex.getErrorCode().getMessage(), ex.getParams());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(BaseResponse.error(ex.getErrorCode(), message, ex.getParams()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Method argument not valid: {}", ex.getMessage());

        Map<String, Object> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String messageKey = fieldError.getDefaultMessage();
            ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
            Map<String, Object> attributes = violation.getConstraintDescriptor().getAttributes();

            String localizedMessage = localizationUtils.getLocalizedMessage(messageKey, attributes);
            errors.put(fieldError.getField(), localizedMessage);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, "Validation failed", errors));
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        log.error("Invalid JPA usage", ex);
        String message = localizationUtils.getLocalizedMessage(ErrorCode.PERSISTENCE_STATE_ERROR.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(ErrorCode.PERSISTENCE_STATE_ERROR, message, null));
    }
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<?>> handleBindException(
            BindException ex) {

        String message = ex.getAllErrors()
                .get(0)
                .getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, message, null));
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<?>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        return ResponseEntity.badRequest()
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, ex.getMessage(), null));
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("No resource found: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.RESOURCE_NOT_FOUND.getMessage());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(BaseResponse.error(ErrorCode.RESOURCE_NOT_FOUND, message, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.VALIDATION_FAILED.getMessage());
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Http message not readable: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.JSON_WRONG_FORMAT.getMessage());
        return ResponseEntity
                .status(ErrorCode.JSON_WRONG_FORMAT.getHttpStatus())
                .body(BaseResponse.error(ErrorCode.JSON_WRONG_FORMAT, message, null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
        if (ex.getCause() instanceof ConstraintViolationException sqlEx) {
            String message = sqlEx.getMessage().toLowerCase();
            if (message.contains("duplicate entry")) {
                errorCode = ErrorCode.DUPLICATE_RESOURCE;
            } else if (message.contains("foreign key")) {
                errorCode = ErrorCode.FOREIGN_KEY_VIOLATION;
            }
        }
        String message = localizationUtils.getLocalizedMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(BaseResponse.error(errorCode, message, null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.FORBIDDEN.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseResponse.error(ErrorCode.FORBIDDEN, message, null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.UNAUTHORIZED.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResponse.error(ErrorCode.UNAUTHORIZED, message, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        String message = localizationUtils.getLocalizedMessage(ErrorCode.SYSTEM_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.error(ErrorCode.SYSTEM_ERROR, message, null));
    }

    @ExceptionHandler(SocketException.class)
    public ResponseEntity<BaseResponse<Void>> handleSocketException(SocketException ex) {
        log.warn("[NETWORK] Socket error: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(BaseResponse.error(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, message, null));
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<BaseResponse<Void>> handleWebClientRequestException(WebClientRequestException ex) {
        log.warn("[PAYMENT] PayOS connection error: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.PAYMENT_PROVIDER_UNAVAILABLE.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(BaseResponse.error(ErrorCode.PAYMENT_PROVIDER_UNAVAILABLE, message, null));
    }

    @ExceptionHandler(NoSuchMessageException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoSuchMessageException(NoSuchMessageException ex) {
        log.error("Missing i18n message key", ex);
        String message = "(System) The message for key '" + ex.getMessage() + "' was not found.";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(ErrorCode.I18N_NO_MESSAGE_FOUND, message, Map.of("messageKey", ex.getMessage())));
    }
}
