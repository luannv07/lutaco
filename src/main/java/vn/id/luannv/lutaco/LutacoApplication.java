package vn.id.luannv.lutaco;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class LutacoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LutacoApplication.class, args);
//        String dataToSign = "amount=5000&cancelUrl=https://www.facebook.com/&description=hello123&orderCode=1&returnUrl=https://www.facebook.com/luanlnv";
//
//        String checksumKey = "ac0d59c211741f93552b8511fdff4af7aa5a33034df877b8899d2c3b16954ed9";
//
//        String signature = PayOSSignature.hmacSha256(dataToSign, checksumKey);
//        log.info(signature);
    }

}
