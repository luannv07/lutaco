package vn.id.luannv.lutaco.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtUtils {
    public static String resolveToken(HttpServletRequest req) {
        if (req.getHeader("Authorization") != null)
            return req.getHeader("Authorization").substring("Bearer ".length());
        return "";
    }
}
