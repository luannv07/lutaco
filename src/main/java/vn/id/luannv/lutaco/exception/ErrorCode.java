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

    // =====================================================
    // ================ SYSTEM / INFRA =====================
    // =====================================================
    SYSTEM_ERROR("system.error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("database.error", HttpStatus.INTERNAL_SERVER_ERROR),
    PERSISTENCE_STATE_ERROR("persistence.state.error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNSUPPORTED_YET("system.unsupported", HttpStatus.BAD_REQUEST),
    I18N_NO_MESSAGE_FOUND("i18n.no.message", HttpStatus.INTERNAL_SERVER_ERROR),

    // =====================================================
    // ================= AUTH / SECURITY ==================
    // =====================================================
    UNAUTHORIZED("auth.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("auth.forbidden", HttpStatus.FORBIDDEN),
    LOGIN_FAILED("auth.login.failed", HttpStatus.BAD_REQUEST),
    INVALID_SIGNATURE("security.invalid.signature", HttpStatus.BAD_REQUEST),

    // =====================================================
    // ============== VALIDATION / INPUT ==================
    // =====================================================
    VALIDATION_FAILED("validation.failed", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING("validation.required", HttpStatus.BAD_REQUEST),
    INVALID_PARAMS("validation.invalid.param", HttpStatus.BAD_REQUEST),
    FIELD_TOO_LONG("validation.field.tooLong", HttpStatus.BAD_REQUEST),
    FIELD_TOO_SHORT("validation.field.tooShort", HttpStatus.BAD_REQUEST),
    FIELD_EXISTED("validation.field.existed", HttpStatus.BAD_REQUEST),
    ENUM_NOT_FOUND("validation.enum.notFound", HttpStatus.BAD_REQUEST),

    // =====================================================
    // ================= ENTITY / RESOURCE =================
    // =====================================================
    ENTITY_NOT_FOUND("entity.not.found", HttpStatus.NOT_FOUND),
    ENTITY_EXISTED("entity.already.exists", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("resource.not.found", HttpStatus.NOT_FOUND),
    RESOURCE_CONFLICT("resource.conflict", HttpStatus.CONFLICT),

    // =====================================================
    // ================= DATABASE CONSTRAINT ===============
    // =====================================================
    DUPLICATE_RESOURCE("db.duplicate", HttpStatus.CONFLICT),
    FOREIGN_KEY_VIOLATION("db.foreign.key.violation", HttpStatus.BAD_REQUEST),

    // =====================================================
    // ================= BUSINESS RULE =====================
    // =====================================================
    OPERATION_NOT_ALLOWED("business.operation.not.allowed", HttpStatus.BAD_REQUEST),
    OPERATION_LIMIT_EXCEEDED("error.plan.limit.exceeded", HttpStatus.BAD_REQUEST),

    // =====================================================
    // ================= PASSWORD DOMAIN ===================
    // =====================================================
    INVALID_OLD_PASSWORD("password.old.invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_NOT_MATCH("password.confirm.not.match", HttpStatus.BAD_REQUEST),

    // =====================================================
    // ===================== OTP SEND ======================
    // =====================================================
    OTP_SEND_FAILED("otp.send.failed", HttpStatus.BAD_REQUEST),
    OTP_SEND_PREVENT("otp.send.prevent", HttpStatus.BAD_REQUEST),
    OTP_SEND_LIMIT_EXCEEDED("otp.send.limit.exceeded", HttpStatus.TOO_MANY_REQUESTS),

    // =====================================================
    // ===================== OTP VERIFY ====================
    // =====================================================
    OTP_INVALID("otp.invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("otp.expired", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPT("otp.max.attempt", HttpStatus.BAD_REQUEST),
    OTP_ALREADY_VERIFIED("otp.already.verified", HttpStatus.BAD_REQUEST),

    // =====================================================
    // ================= PAYMENT / EXTERNAL =================
    // =====================================================
    PAYMENT_PROVIDER_ERROR("payment.provider.error", HttpStatus.BAD_REQUEST),

    // xử lý chung cho error code trả về từ PayOS / external payment
    PAYMENT_SYSTEM_ERROR("payment.system.error", HttpStatus.BAD_REQUEST),

    PAYMENT_PROVIDER_UNAVAILABLE("payment.provider.unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    EXTERNAL_SERVICE_UNAVAILABLE("external.service.unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // =====================================================
    // ===================== JSON / FORMAT ==================
    // =====================================================
    JSON_WRONG_FORMAT("json.wrong.format", HttpStatus.BAD_REQUEST),

    // =====================================================
    // ===================== JOB / SCHEDULER ================
    // =====================================================
    JOB_INTERRUPTED("job.interrupted", HttpStatus.BAD_REQUEST), PLAN_NOT_CONFIGURED("error.plan.not.configured", HttpStatus.BAD_REQUEST);

    // =====================================================
    // ===================== FIELDS =========================
    // =====================================================
    String message;
    HttpStatus httpStatus;
}