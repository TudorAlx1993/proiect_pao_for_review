package configs;

public final class FeesConfig {
    private static double atmWithdrawFeePercent;
    private static double internalPaymentFeePercent;
    private static double externalPaymentFeePercent;
    private static final double minFeePercent;
    private static final double maxFeePercent;

    static {
        atmWithdrawFeePercent = 0.0;
        internalPaymentFeePercent = 0.0;
        externalPaymentFeePercent = 0.0;

        minFeePercent = 0.0;
        maxFeePercent = 100.0;
    }

    private FeesConfig(){

    }

    public static double getMinFeePercent(){
        return FeesConfig.minFeePercent;
    }

    public static double getMaxFeePercent(){
        return FeesConfig.maxFeePercent;
    }

    public static double getAtmWitdrawFeePercent() {
        return FeesConfig.atmWithdrawFeePercent;
    }

    public static double getInternalPaymentFeePercent() {
        return FeesConfig.internalPaymentFeePercent;
    }

    public static double getExternalPaymentFeePercent() {
        return FeesConfig.externalPaymentFeePercent;
    }

    public static void setAtmWitdrawFeePercent(double atmWitdrawFeePercent) {
        if (atmWitdrawFeePercent < FeesConfig.minFeePercent ||
                atmWitdrawFeePercent > FeesConfig.maxFeePercent)
            return;

        FeesConfig.atmWithdrawFeePercent = atmWitdrawFeePercent;
    }

    public static void setInternalPaymentFeePercent(double internalPaymentFeePercent) {
        if (internalPaymentFeePercent < FeesConfig.minFeePercent ||
                internalPaymentFeePercent > FeesConfig.maxFeePercent)
            return;

        FeesConfig.internalPaymentFeePercent = internalPaymentFeePercent;
    }

    public static void setExternalPaymentFeePercent(double externalPaymentFeePercent) {
        if (externalPaymentFeePercent < FeesConfig.minFeePercent ||
                externalPaymentFeePercent > FeesConfig.maxFeePercent)
            return;

        FeesConfig.externalPaymentFeePercent = externalPaymentFeePercent;
    }
}
