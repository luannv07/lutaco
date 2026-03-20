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
public enum UserPlan {
    PREMIUM("config.enum.user.plan.premium"),
    FREEMIUM("config.enum.user.plan.freemium");

    String display;
    @NonFinal
    int maxWallets;
    @NonFinal
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
