package configs;

public final class IBANConfig {
    private static final int ibanLength;

    static {
        ibanLength = 34;
    }

    private IBANConfig() {
    }

    public static int getIbanLength() {
        return IBANConfig.ibanLength;
    }
}
