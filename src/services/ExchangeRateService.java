package services;

import configs.Codes;
import configs.ExchangeRatesConfig;
import currency.Currency;
import exceptions.InvalidExchangePairException;

import java.util.Map;
import java.util.Objects;

public class ExchangeRateService extends Service {
    private final Currency fromCurrency;
    private final Currency toCurrency;

    public ExchangeRateService(Currency fromCurrency,
                        Currency toCurrency) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;

        try {
            this.checkExchangePair(fromCurrency, toCurrency);
        } catch (InvalidExchangePairException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    public double getAmountInPairedCurrency(double amount) {
        double amountInPairedCurrency;
        if (this.fromCurrency.equals(ExchangeRatesConfig.getBaseCurrency())) {
            // we sell RON against the other currencies
            double spread = ExchangeRatesConfig.getAskSpreadPercent() / 100;
            double referenceExchangeRate = ExchangeRatesConfig.getReferenceExchangeRateOfCurrencyPerRON(toCurrency).doubleValue();
            double actualExchangeRate = referenceExchangeRate * (1 + spread);
            amountInPairedCurrency = amount / actualExchangeRate;
        } else {
            // we buy RON against the other currencies
            double spread = ExchangeRatesConfig.getBidSpreadPercent() / 100;
            double referenceExchangeRate = ExchangeRatesConfig.getReferenceExchangeRateOfCurrencyPerRON(fromCurrency).doubleValue();
            double actualExchangeRate = referenceExchangeRate * (1 - spread);
            amountInPairedCurrency = amount * actualExchangeRate;
        }

        return amountInPairedCurrency;
    }

    private void checkExchangePair(Currency fromCurrency, Currency toCurrency) throws InvalidExchangePairException {
        if (!fromCurrency.equals(ExchangeRatesConfig.getBaseCurrency()) &&
                !toCurrency.equals(ExchangeRatesConfig.getBaseCurrency()))
            throw new InvalidExchangePairException("Error: the bank allows to buy and sell various currencies only against " +
                    ExchangeRatesConfig.getBaseCurrency().getCurrencyCode() + "!");

        if (fromCurrency.equals(toCurrency))
            throw new InvalidExchangePairException("Error: you cannot change a currency for exactly the same currency!");
    }


    public static void showAvailableExchangeRates() {
        Map<Currency, Double> referenceExchangeRates = ExchangeRatesConfig.getReferenceExchangeRatesPerRON();
        double bidSpreadPercent = ExchangeRatesConfig.getBidSpreadPercent();
        double askSpreadPercent = ExchangeRatesConfig.getAskSpreadPercent();
        StringBuilder information = new StringBuilder();

        information.append("\nExchange rates: \n");
        for (Currency currency : referenceExchangeRates.keySet()) {
            double exchangeRatePerRON = referenceExchangeRates.get(currency);
            double weBuy = exchangeRatePerRON * (1 - bidSpreadPercent / 100);
            double weSell = exchangeRatePerRON * (1 + askSpreadPercent / 100);
            information.append("\t* " + currency.getCurrencyCode() + "/RON:\n" +
                    "\t\twe buy: " + String.format("%.4f", weBuy) + "\n" +
                    "\t\twe sell: " + String.format("%.4f", weSell) + "\n");
        }

        System.out.println(information.toString());
    }

    public Currency getFromCurrency() {
        return this.fromCurrency;
    }

    public Currency getToCurrency() {
        return this.toCurrency;
    }

    @Override
    public String toString() {
        Currency baseCurrency = ExchangeRatesConfig.getBaseCurrency();
        if (this.fromCurrency.equals(baseCurrency))
            return this.toCurrency.getCurrencyCode() +
                    "/" +
                    this.fromCurrency.getCurrencyCode() +
                    " (sell " +
                    baseCurrency.getCurrencyCode() +
                    ")";
        else
            return this.fromCurrency.getCurrencyCode() +
                    "/" +
                    this.toCurrency.getCurrencyCode() +
                    " (buy " +
                    baseCurrency.getCurrencyCode() +
                    ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fromCurrency.hashCode(), this.toCurrency.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof ExchangeRateService))
            return false;

        ExchangeRateService exchangeRateService = (ExchangeRateService) object;
        if (!this.fromCurrency.equals(exchangeRateService.fromCurrency))
            return false;
        if (!this.toCurrency.equals(exchangeRateService.toCurrency))
            return false;

        return true;
    }
}
