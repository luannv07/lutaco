package vn.id.luannv.lutaco.annotation.validate;

import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.annotation.bind.AuditUsername;

import java.lang.reflect.Field;

@Component
public class PublicAuditValidate {
    public String getUsernameFromRequest(JoinPoint jp) {

        for (Object arg : jp.getArgs()) {       // duyệt request
            if (arg == null) continue;

            for (Field field : arg.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(AuditUsername.class)) {
                    field.setAccessible(true);
                    try {
                        return field.get(arg).toString();
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

}
