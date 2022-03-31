package configs;

public final class DepositConfig {
    private static final int depositUniqueIdLength;

    static {
        depositUniqueIdLength = 20;
    }

    private DepositConfig() {

    }

    public static int getDepositUniqueIdLength() {
        return DepositConfig.depositUniqueIdLength;
    }
}
