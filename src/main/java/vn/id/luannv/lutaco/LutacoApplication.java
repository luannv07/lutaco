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
        long start = System.currentTimeMillis();
        SpringApplication.run(LutacoApplication.class, args);
        log.info("✅ Build successfully! Time execute: {}s", (System.currentTimeMillis() - start * 1.0) / 1000);
    }

}
