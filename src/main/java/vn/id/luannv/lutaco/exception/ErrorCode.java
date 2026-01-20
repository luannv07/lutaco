package vn.id.luannv.lutaco.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    // System errors
    SYSTEM_ERROR("sys.error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("db.error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", HttpStatus.FORBIDDEN),

    // Validation / Input errors
    VALIDATION_FAILED("input.invalid", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING("input.required", HttpStatus.BAD_REQUEST),
    FIELD_TOO_LONG("input.tooLong", HttpStatus.BAD_REQUEST),
    FIELD_TOO_SHORT("input.tooShort", HttpStatus.BAD_REQUEST),
    FIELD_EXISTS("input.fieldExists", HttpStatus.BAD_REQUEST),

    // Generic entity errors
    ENTITY_NOT_FOUND("entity.notFound", HttpStatus.NOT_FOUND),
    ENTITY_EXISTS("entity.exists", HttpStatus.BAD_REQUEST),

    // Business rules
    OPERATION_NOT_ALLOWED("operation.notAllowed", HttpStatus.BAD_REQUEST),
    RESOURCE_CONFLICT("resource.conflict", HttpStatus.CONFLICT),
    RESOURCE_NOT_FOUND("resource.notFound", HttpStatus.NOT_FOUND),

    JSON_WRONG_FORMAT("json.wrong.format", HttpStatus.BAD_REQUEST),
    // db keys
    DUPLICATE_RESOURCE("db.duplicate", HttpStatus.CONFLICT),
    FOREIGN_KEY_VIOLATION("db.foreignKeyViolation", HttpStatus.BAD_REQUEST),

    LOGIN_FAILED("authenticate.failed", HttpStatus.BAD_REQUEST),
    ENUM_NOT_FOUND("enum.notFound", HttpStatus.BAD_REQUEST),

    // password domain
    INVALID_OLD_PASSWORD("password.old.invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_NOT_MATCH("password.confirm.notMatch", HttpStatus.BAD_REQUEST),

    // ===== OTP SEND =====
    OTP_SEND_FAILED("otp.send.failed", HttpStatus.BAD_REQUEST),
    OTP_SEND_PREVENT("otp.send.prevent", HttpStatus.BAD_REQUEST),
    OTP_SEND_LIMIT_EXCEEDED("otp.send.limitExceeded", HttpStatus.TOO_MANY_REQUESTS),

    // ===== OTP VERIFY =====
    OTP_INVALID("otp.invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("otp.expired", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPT("otp.maxAttempt", HttpStatus.BAD_REQUEST),
    OTP_ALREADY_VERIFIED("otp.alreadyVerified", HttpStatus.BAD_REQUEST),
    ;

    String message;
    HttpStatus httpStatus;
}
