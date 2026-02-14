package vn.id.luannv.lutaco.enumerate;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum UserPlan {
    PREMIUM,
    FREEMIUM;

    int maxWallets;
    int maxBudgetsCount;

    @Component
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class UserPlanInitializer {

        @Value("${plan.premium.max-wallets}")
        int premiumMaxWallets;

        @Value("${plan.premium.max-budgets}")
        int premiumMaxBudgets;

        @Value("${plan.freemium.max-wallets}")
        int freemiumMaxWallets;

        @Value("${plan.freemium.max-budgets}")
        int freemiumMaxBudgets;

        @PostConstruct
        public void init() {
            PREMIUM.maxWallets = premiumMaxWallets;
            PREMIUM.maxBudgetsCount = premiumMaxBudgets;
            FREEMIUM.maxWallets = freemiumMaxWallets;
            FREEMIUM.maxBudgetsCount = freemiumMaxBudgets;
        }
    }
}
