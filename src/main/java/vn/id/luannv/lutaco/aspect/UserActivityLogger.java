package vn.id.luannv.lutaco.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.id.luannv.lutaco.annotation.validate.PublicAuditValidate;
import vn.id.luannv.lutaco.event.entity.UserAuditEvent;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserActivityLogger {
    ApplicationEventPublisher applicationEventPublisher;
    PublicAuditValidate publicAuditValidate;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {
    }

    @Around("restController()")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        long start = System.nanoTime();

        String username = "anonymous"; // Giá trị mặc định nếu không xác định được ai

        try {
            // Kiểm tra xem SecurityContext có Authentication không và có phải String không
            String currentUsername = SecurityUtils.getCurrentUsername();
            if (currentUsername != null && !currentUsername.isEmpty()) {
                username = currentUsername;
            }
        } catch (Exception e) {
            // Log nhẹ để debug nếu cần, nhưng không được để crash
            log.warn("Could not get username from SecurityContext, trying PublicAuditValidate...");
            try {
                String auditUsername = publicAuditValidate.getUsernameFromRequest(joinPoint);
                if (auditUsername != null) {
                    username = auditUsername;
                }
            } catch (Exception ex) {
                log.error("Failed to get username even from request: {}", ex.getMessage());
            }
        }

        // Luôn luôn thực thi logic chính của Controller
        Object result = joinPoint.proceed();

        long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // Bọc việc publish event vào try-catch để chắc chắn AI trả về kết quả kể cả khi lưu log lỗi
        try {
            applicationEventPublisher.publishEvent(
                    UserAuditEvent.builder()
                            .username(username)
                            .userAgent(request.getHeader("User-Agent"))
                            .clientIp(SecurityUtils.resolveClientIp(request))
                            .requestUri(request.getRequestURI())
                            .methodName(
                                    joinPoint.getSignature().getDeclaringTypeName()
                                            + "." + joinPoint.getSignature().getName()
                            )
                            .executionTimeMs(executionTime)
                            .paramContent(request.getQueryString())
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to publish UserAuditEvent: {}", e.getMessage());
        }

        return result;
    }


}




