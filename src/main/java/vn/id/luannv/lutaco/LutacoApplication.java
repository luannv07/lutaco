package vn.id.luannv.lutaco;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import vn.id.luannv.lutaco.enumerate.UserPlan;

import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class LutacoApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(LutacoApplication.class, args);
        log.info("✅ Build Successfully!");
    }

}
