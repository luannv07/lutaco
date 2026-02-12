package vn.id.luannv.lutaco.enumerate;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum UserPlan {
    PREMIUM,
    FREEMIUM;

    int maxWallets;
    int maxBudgetCount;

    @Component
    public static class ValueInjector {

        @Value("${user-plan.premium-max-wallets}")
        private int premiumMaxWallets;

        @Value("${user-plan.premium-max-budget-count}")
        private int premiumMaxBudgetCount;

        @Value("${user-plan.freemium-max-wallets}")
        private int freemiumMaxWallets;

        @Value("${user-plan.freemium-max-budget-count}")
        private int freemiumMaxBudgetCount;

        @PostConstruct
        public void postConstruct() {
            PREMIUM.maxWallets = premiumMaxWallets;
            PREMIUM.maxBudgetCount = premiumMaxBudgetCount;
            FREEMIUM.maxWallets = freemiumMaxWallets;
            FREEMIUM.maxBudgetCount = freemiumMaxBudgetCount;
        }
    }
}
