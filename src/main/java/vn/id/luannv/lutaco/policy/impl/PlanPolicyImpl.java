package vn.id.luannv.lutaco.policy.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.policy.PlanPolicy;

@Component
public class PlanPolicyImpl implements PlanPolicy {

    @Value("${plan.freemium.max-wallets}")
    private int freemiumMaxWallets;

    @Value("${plan.premium.max-wallets}")
    private int premiumMaxWallets;

    @Value("${plan.freemium.max-budgets}")
    private int freemiumMaxBudgets;

    @Value("${plan.premium.max-budgets}")
    private int premiumMaxBudgets;

    @Override
    public int maxWallets(User user) {
        return switch (user.getUserPlan()) {
            case PREMIUM -> premiumMaxWallets;
            default -> freemiumMaxWallets;
        };
    }

    @Override
    public boolean canCreateWallet(User user, int currentWalletCount) {
        return currentWalletCount < maxWallets(user);
    }

    @Override
    public int maxBudgets(User user) {
        return switch (user.getUserPlan()) {
            case PREMIUM -> premiumMaxBudgets;
            default -> freemiumMaxBudgets;
        };
    }

    @Override
    public boolean canCreateBudget(User user, int currentBudgetCount) {
        return currentBudgetCount < maxBudgets(user);
    }
}
