package vn.id.luannv.lutaco;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class LutacoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LutacoApplication.class, args);
    }

}
