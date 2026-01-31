package vn.id.luannv.lutaco.enumerate;

import java.util.Arrays;

public enum TransactionType {
    INCOME,
    EXPENSE;

    public static boolean isValidTransactionType(String transType) {
        return Arrays.stream(TransactionType.values()).anyMatch(tt -> tt.name().equals(transType));
    }
}
