package vn.id.luannv.lutaco.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "dashboard.insight")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InsightThresholdConfig {

    Expense expense = new Expense();
    Income income = new Income();
    Category category = new Category();
    Balance balance = new Balance();

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Expense {
        @DecimalMin(message = "dashboard.message.invalidConfig", value = "0.0")
        @DecimalMax(message = "dashboard.message.invalidConfig", value = "100.0")
        double warnRate;
        @DecimalMin(message = "dashboard.message.invalidConfig", value = "0.0")
        @DecimalMax(message = "dashboard.message.invalidConfig", value = "100.0")
        double dangerRate;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Income {
        @DecimalMin(message = "dashboard.message.invalidConfig", value = "0.0")
        @DecimalMax(message = "dashboard.message.invalidConfig", value = "100.0")
        double successRate;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Category {
        @DecimalMin(message = "dashboard.message.invalidConfig", value = "0.0")
        @DecimalMax(message = "dashboard.message.invalidConfig", value = "100.0")
        double warnRatio;
        @DecimalMin(message = "dashboard.message.invalidConfig", value = "0.0")
        @DecimalMax(message = "dashboard.message.invalidConfig", value = "100.0")
        double dangerRatio;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Balance {
        long negative;
    }
}