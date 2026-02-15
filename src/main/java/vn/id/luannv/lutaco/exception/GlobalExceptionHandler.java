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
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LocalizationUtils localizationUtils;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Caught BusinessException: Code={}, Message={}, Params={}", ex.getErrorCode(), ex.getMessage(), ex.getParams());
        Map<String, Object> params = new HashMap<>();
        if (ex.getParams() != null) {
            ex.getParams().forEach((key, valueObj) -> {
                if (valueObj instanceof String)
                    params.put(key, localizationUtils.getLocalizedMessage(((String) valueObj).toLowerCase()));
            });
        }
        String message = localizationUtils.getLocalizedMessage(ex.getErrorCode().getMessage(), params);

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(BaseResponse.error(ex.getErrorCode(), message, params));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String messageKey = fieldError.getDefaultMessage();
            ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
            Map<String, Object> attributes = violation.getConstraintDescriptor().getAttributes();

                String localizedMessage = localizationUtils.getLocalizedMessage(messageKey, attributes);
            errors.put(fieldError.getField(), localizedMessage);
            log.warn("Validation error in field '{}': {}", fieldError.getField(), localizedMessage);
        }
        log.warn("MethodArgumentNotValidException: {} validation errors occurred.", errors.size());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, "Validation failed", errors));
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        log.error("Invalid JPA data access API usage detected: {}", ex.getMessage(), ex);
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
        log.warn("Data binding error: {}", message);
        return ResponseEntity.badRequest()
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, message, null));
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<?>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch: Parameter '{}' expected type '{}' but received '{}'. Message: {}",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown", ex.getValue(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, ex.getMessage(), null));
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Requested resource not found: {}", ex.getResourcePath());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.RESOURCE_NOT_FOUND.getMessage());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(BaseResponse.error(ErrorCode.RESOURCE_NOT_FOUND, message, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument provided: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.VALIDATION_FAILED.getMessage());
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(BaseResponse.error(ErrorCode.VALIDATION_FAILED, message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("HTTP message not readable (malformed JSON/request body): {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.JSON_WRONG_FORMAT.getMessage());
        return ResponseEntity
                .status(ErrorCode.JSON_WRONG_FORMAT.getHttpStatus())
                .body(BaseResponse.error(ErrorCode.JSON_WRONG_FORMAT, message, null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
        String logMessage = "Data integrity violation occurred.";
        if (ex.getCause() instanceof ConstraintViolationException sqlEx) {
            String message = sqlEx.getMessage().toLowerCase();
            if (message.contains("duplicate entry") || message.contains("unique constraint")) {
                errorCode = ErrorCode.DUPLICATE_RESOURCE;
                logMessage = "Duplicate resource detected: " + sqlEx.getConstraintName();
            } else if (message.contains("foreign key")) {
                errorCode = ErrorCode.FOREIGN_KEY_VIOLATION;
                logMessage = "Foreign key constraint violation: " + sqlEx.getConstraintName();
            }
        }
        log.warn("{}: {}", logMessage, ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(BaseResponse.error(errorCode, message, null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied for current user: {}", ex.getMessage());
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
        log.error("An unexpected internal server error occurred: {}", e.getMessage(), e);
        String message = localizationUtils.getLocalizedMessage(ErrorCode.SYSTEM_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.error(ErrorCode.SYSTEM_ERROR, message, null));
    }

    @ExceptionHandler(SocketException.class)
    public ResponseEntity<BaseResponse<Void>> handleSocketException(SocketException ex) {
        log.warn("Network communication error (SocketException): {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(BaseResponse.error(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, message, null));
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<BaseResponse<Void>> handleWebClientRequestException(WebClientRequestException ex) {
        log.warn("External service connection error (WebClientRequestException), possibly PayOS: {}", ex.getMessage());
        String message = localizationUtils.getLocalizedMessage(ErrorCode.PAYMENT_PROVIDER_UNAVAILABLE.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(BaseResponse.error(ErrorCode.PAYMENT_PROVIDER_UNAVAILABLE, message, null));
    }

    @ExceptionHandler(NoSuchMessageException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoSuchMessageException(NoSuchMessageException ex) {
        log.error("Missing i18n message key in localization bundle: {}", ex.getMessage(), ex);
        String message = "(System) The message for key '" + ex.getMessage() + "' was not found.";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(ErrorCode.I18N_NO_MESSAGE_FOUND, message, Map.of("messageKey", ex.getMessage())));
    }
}
