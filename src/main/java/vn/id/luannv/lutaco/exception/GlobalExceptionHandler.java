package vn.id.luannv.lutaco.exception;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.id.luannv.lutaco.dto.response.BaseResponse;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j

public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException ex) {
        log.warn("Business error: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(BaseResponse
                        .error(ex.getParams(), ex.getErrorCode()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("No resource found: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(BaseResponse
                        .error(Map.of(), ErrorCode.RESOURCE_NOT_FOUND));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Method argument not valid: {}", ex.getMessage());
        Map<String, Object> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors().forEach(error -> {
                    Map<String, Object> fieldErrors = new HashMap<>();

                    fieldErrors.put("field", error.getField());
                    fieldErrors.put("messageKey", error.getDefaultMessage());

                    final List<String> EXCLUDED_KEYS = List.of("message", "payload", "groups","regexp", "flags");
                    ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);

                    Map<String, Object> violationErrors = violation.getConstraintDescriptor().getAttributes()
                            .entrySet()
                            .stream()
                            .filter(stringObjectEntry -> !EXCLUDED_KEYS.contains(stringObjectEntry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    fieldErrors.put("constraints", violationErrors);

                    errors.put(error.getField(), fieldErrors);
                });

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(BaseResponse
                        .error(errors, ErrorCode.VALIDATION_FAILED));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {} {}", ex, ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(BaseResponse.error(
                        Map.of(),
                        ErrorCode.VALIDATION_FAILED
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Http message not readable: {}", ex.getMessage());

        return ResponseEntity
                .status(ErrorCode.JSON_WRONG_FORMAT.getHttpStatus())
                .body(BaseResponse.error(
                        Map.of(),
                        ErrorCode.JSON_WRONG_FORMAT
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());

        Throwable throwable = ex.getCause();

        if (throwable instanceof ConstraintViolationException sqlEx) {
            String message = sqlEx.getMessage();

            if (message.contains("Duplicate entry"))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        BaseResponse.error(
                                Map.of(),
                                ErrorCode.DUPLICATE_RESOURCE
                        ));

            if (message.contains("foreign key"))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        BaseResponse.error(
                                Map.of(),
                                ErrorCode.FOREIGN_KEY_VIOLATION
                        ));
        }

        return ResponseEntity.status(ErrorCode.DATABASE_ERROR.getHttpStatus()).body(
                BaseResponse.error(
                        Map.of(),
                        ErrorCode.DATABASE_ERROR
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        BaseResponse<Object> response = BaseResponse.error(Map.of(), ErrorCode.FORBIDDEN);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        BaseResponse<Object> response = BaseResponse.error(Map.of(), ErrorCode.UNAUTHORIZED);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("Exception: {}", e.getStackTrace(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                BaseResponse.error(
                        Map.of(),
                        ErrorCode.SYSTEM_ERROR
                )
        );
    }


}


