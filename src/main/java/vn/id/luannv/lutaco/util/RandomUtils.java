package vn.id.luannv.lutaco.util;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generateOtp() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 999999)
        );
    }

    public static String randomAlphaNum(int len) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

}
