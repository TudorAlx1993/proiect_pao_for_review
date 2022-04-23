package configs;

public final class CustomerConfig {
    private static final String cuiPattern;
    private static final String cnpPattern;
    private static final String hashAlgorithm;
    private static final String passwordPattern;

    static {
        cuiPattern = "[0-9]{6}";
        cnpPattern = "[0-9]{13}";
        hashAlgorithm = "SHA-256";
        passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{10,20}$";
    }

    private CustomerConfig() {

    }

    public static String getCuiPattern() {
        return CustomerConfig.cuiPattern;
    }

    public static String getCnpPattern() {
        return CustomerConfig.cnpPattern;
    }

    public static String getHashAlgorithm() {
        return CustomerConfig.hashAlgorithm;
    }

    public static String getPasswordPattern() {
        return CustomerConfig.passwordPattern;
    }
}
