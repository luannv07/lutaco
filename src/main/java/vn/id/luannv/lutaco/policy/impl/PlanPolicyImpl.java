package vn.id.luannv.lutaco.policy.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.policy.PlanPolicy;

@Component
public class PlanPolicyImpl implements PlanPolicy {

    @Value("${plan.freemium.max-wallets}")
    private int freemiumMax;

    @Value("${plan.premium.max-wallets}")
    private int premiumMax;

    @Override
    public int maxWallets(User user) {
        return switch (user.getUserPlan()) {
            case PREMIUM -> premiumMax;
            default -> freemiumMax;
        };
    }

    @Override
    public boolean canCreateWallet(User user, int currentWalletCount) {
        return currentWalletCount < maxWallets(user);
    }
}
