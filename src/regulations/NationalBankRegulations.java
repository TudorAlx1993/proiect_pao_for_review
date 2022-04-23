package regulations;

import java.util.*;

public final class NationalBankRegulations {
    private final static Map<String, String> allowedCurrencies;
    private final static String[] networkProcessors;
    private static final double maxThresholdDebtRatioIndividual;
    private static final double maxThresholdDebtRatioCompany;

    static {
        allowedCurrencies = new HashMap<String, String>();
        allowedCurrencies.put("USD", "US Dollar");
        allowedCurrencies.put("RON", "Romanian Leu");
        allowedCurrencies.put("EUR", "Euro");
        allowedCurrencies.put("CHF", "Swiss Franc");

        networkProcessors = new String[]{"Visa", "Mastercard", "Maestro"};

        maxThresholdDebtRatioIndividual = 0.5;
        maxThresholdDebtRatioCompany = 1.0;
    }

    private NationalBankRegulations() {

    }

    public static String[] getAllowedCurrencyCodes() {
        return NationalBankRegulations.allowedCurrencies.keySet().toArray(new String[NationalBankRegulations.allowedCurrencies.size()]);
    }

    public static String getCurrencyOfficialName(String currencyCode) {
        return NationalBankRegulations.allowedCurrencies.get(currencyCode);
    }

    public static String[] getNetworkProcessors() {
        // make sure you don't modify the regulation from outside
        return Arrays.copyOf(NationalBankRegulations.networkProcessors,
                NationalBankRegulations.networkProcessors.length);
    }

    public static double getMaxThresholdDebtRatioIndividual() {
        return NationalBankRegulations.maxThresholdDebtRatioIndividual;
    }

    public static double getMaxThresholdDebtRatioCompany() {
        return NationalBankRegulations.maxThresholdDebtRatioCompany;
    }
}
