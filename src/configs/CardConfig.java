package configs;

public final class CardConfig {
    private static final int cardNumberLength;
    private static final int yearsOfValability;
    private static final String pinHashAlg;
    private static final String pinPattern;

    static {
        cardNumberLength = 16;
        yearsOfValability = 3;
        pinHashAlg = "SHA-256";
        pinPattern = "[0-9]{4}";
    }

    private CardConfig() {
    }

    public static int getCardNumberLength() {
        return CardConfig.cardNumberLength;
    }

    public static int getYearsOfValability() {
        return CardConfig.yearsOfValability;
    }

    public static String getPinPattern() {
        return CardConfig.pinPattern;
    }

    public static String getPinHashAlg() {
        return CardConfig.pinHashAlg;
    }
}
