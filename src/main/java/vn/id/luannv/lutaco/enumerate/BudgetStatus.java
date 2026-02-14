package vn.id.luannv.lutaco.enumerate;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum BudgetStatus {
    NORMAL,
    WARNING,
    DANGER,
    UNKNOWN; // tu choi gui mail

    int percentage;

    @Component
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class UserPlanInitializer {

        @Value("${budget.status.normal}")
        int normal;
        @Value("${budget.status.warning}")
        int warning;
        @Value("${budget.status.danger}")
        int danger;

        @PostConstruct
        public void init() {
            NORMAL.percentage = normal;
            WARNING.percentage = warning;
            DANGER.percentage = danger;
            UNKNOWN.percentage = -1;
        }
    }
}
