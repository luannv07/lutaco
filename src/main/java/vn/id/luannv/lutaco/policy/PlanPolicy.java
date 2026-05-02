package vn.id.luannv.lutaco.policy;

import vn.id.luannv.lutaco.entity.User;

public interface PlanPolicy {
    int maxWallets(User user);

    boolean canCreateWallet(User user, int currentWalletCount);
}
