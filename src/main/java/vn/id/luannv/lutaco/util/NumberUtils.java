package vn.id.luannv.lutaco.util;

import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {
    public static String generateOtp() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 999999)
        );
    }
}
