package vn.id.luannv.lutaco.policy;

import vn.id.luannv.lutaco.entity.User;

public interface PlanPolicy {
    int maxWallets(User user);

    boolean canCreateWallet(User user, int currentWalletCount);

    int maxBudgets(User user);

    boolean canCreateBudget(User user, int currentBudgetCount);
}
