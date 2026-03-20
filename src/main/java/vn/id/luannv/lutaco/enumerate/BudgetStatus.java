package vn.id.luannv.lutaco.enumerate;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@RequiredArgsConstructor
public enum BudgetStatus {
    NORMAL("config.enum.budget.status.normal"),
    WARNING("config.enum.budget.status.warning"),
    DANGER("config.enum.budget.status.danger"),
    UNKNOWN("config.enum.budget.status.unknown"); // tu choi gui mail

    String display;
    @NonFinal
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
