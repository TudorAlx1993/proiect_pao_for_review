package configs;

import currency.Currency;

import java.util.HashMap;
import java.util.Map;

public final class ExchangeRatesConfig {
    private static final Map<Currency, Double> referenceExchangeRates;
    private static double bidSpreadPercent;
    private static double askSpreadPercent;
    private static final double minSpreadPercent;
    private static final double maxSpreadPercent;
    private static final Currency baseCurrency;

    static {
        referenceExchangeRates = new HashMap<Currency, Double>();
        bidSpreadPercent = 0.0;
        askSpreadPercent = 0.0;
        minSpreadPercent = 0.0;
        maxSpreadPercent = 100.0;
        baseCurrency = new Currency("RON");
    }

    private ExchangeRatesConfig() {

    }

    public static double getMinSpreadPercent(){
        return ExchangeRatesConfig.minSpreadPercent;
    }

    public static double getMaxSpreadPercent(){
        return ExchangeRatesConfig.maxSpreadPercent;
    }

    public static void setReferenceExchangeRateOfCurrencyPerRON(Currency currency, double exchangeRate) {
        ExchangeRatesConfig.referenceExchangeRates.put(currency, Double.valueOf(exchangeRate));
    }

    public static Double getReferenceExchangeRateOfCurrencyPerRON(Currency currency) {
        return ExchangeRatesConfig.referenceExchangeRates.get(currency);
    }

    public static Map<Currency, Double> getReferenceExchangeRatesPerRON() {
        return ExchangeRatesConfig.referenceExchangeRates;
    }

    public static double getBidSpreadPercent() {
        return ExchangeRatesConfig.bidSpreadPercent;
    }

    public static void setBidSpreadPercent(double bidSpreadPercent) {
        ExchangeRatesConfig.bidSpreadPercent = bidSpreadPercent;
    }

    public static double getAskSpreadPercent() {
        return ExchangeRatesConfig.askSpreadPercent;
    }

    public static void setAskSpreadPercent(double askSpreadPercent) {
        ExchangeRatesConfig.askSpreadPercent = askSpreadPercent;
    }

    public static Currency getBaseCurrency() {
        return ExchangeRatesConfig.baseCurrency;
    }
}
