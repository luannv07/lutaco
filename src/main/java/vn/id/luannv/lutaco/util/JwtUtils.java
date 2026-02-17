package vn.id.luannv.lutaco.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtUtils {
    public static String resolveToken(HttpServletRequest req) {
        String authorizationHeader = req.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length());
        }
        log.debug("[system]: Authorization header not found or does not start with 'Bearer ' in request.");
        return "";
    }
}
