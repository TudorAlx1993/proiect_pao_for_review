package configs;

import audit.AuditService;
import audit.UserType;
import currency.Currency;
import exceptions.InvalidDepositMaturityException;
import exceptions.InvalidInterestRateException;

import java.util.*;

final class Maturity implements Comparable<Maturity> {
    // maturitatea este in nr luni
    private final int maturity;

    public Maturity(int maturity) {
        this.maturity = maturity;
    }

    public int getMaturity() {
        return this.maturity;
    }

    @Override
    public int compareTo(Maturity maturity) {
        return this.maturity - maturity.getMaturity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.maturity);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        Maturity maturity = (Maturity) object;
        if (this.maturity != maturity.maturity)
            return false;

        return true;
    }
}

public final class InterestRateConfig {
    private static final Map<Currency, HashMap<Maturity, Double>> depositInterestRate;
    // for loans the interest rate is set only with respect to te currency
    private static final Map<Currency, Double> loanInterestRate;
    private static final int minInterestRate;
    private static final int maxInterestRate;
    // maturity in months
    private static final int[] allowedMaturitiesDeposit;

    static {
        depositInterestRate = new HashMap<Currency, HashMap<Maturity, Double>>();
        loanInterestRate = new HashMap<Currency, Double>();
        minInterestRate = 0;
        maxInterestRate = 100;
        allowedMaturitiesDeposit = new int[]{1, 3, 6, 9, 12};
    }

    private InterestRateConfig() {

    }

    private static void validateInterestRate(double interestRate) throws InvalidInterestRateException {
        if (interestRate < InterestRateConfig.minInterestRate ||
                interestRate > InterestRateConfig.maxInterestRate)
            throw new InvalidInterestRateException("Error: the interest rate should be between " +
                    InterestRateConfig.minInterestRate + "% and " +
                    InterestRateConfig.maxInterestRate + "%!");
    }

    private static void validateDepositMaturity(int maturity) throws InvalidDepositMaturityException {
        for (int allowedMaturity : InterestRateConfig.allowedMaturitiesDeposit)
            if (maturity == allowedMaturity)
                return;

        throw new InvalidDepositMaturityException("Error: the allowed maturities for deposits are: " +
                Arrays.toString(InterestRateConfig.allowedMaturitiesDeposit) +
                " months!");
    }

    private static void validateUserInputs(double interestRate, Maturity maturity) {
        try {
            InterestRateConfig.validateInterestRate(interestRate);
            if (maturity != null)
                InterestRateConfig.validateDepositMaturity(maturity.getMaturity());
        } catch (InvalidInterestRateException |
                InvalidDepositMaturityException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    public static void setLoanInterestRate(Currency currency, double interestRate) {
        InterestRateConfig.validateUserInputs(interestRate, null);

        InterestRateConfig.loanInterestRate.put(currency, Double.valueOf(interestRate));
    }

    public static Double getLoanInterestRate(Currency currency) {
        // if the key does not exist it will return null
        return InterestRateConfig.loanInterestRate.get(currency);
    }

    public static void showInterestRates() {
        if (InterestRateConfig.loanInterestRate.size() != 0) {
            System.out.println("\nLoan interest rates:");
            for (Currency currency : InterestRateConfig.loanInterestRate.keySet())
                System.out.println("\t* " + currency.getCurrencyCode() + ": " +
                        InterestRateConfig.loanInterestRate.get(currency) + "%");
        } else
            System.out.println("\nThe interest rates for loans are not yet set!");

        if (InterestRateConfig.depositInterestRate.size() != 0) {
            System.out.println("\nDeposit interest rates:");
            for (Currency currency : InterestRateConfig.depositInterestRate.keySet()) {
                System.out.println("\t* " + currency.getCurrencyCode() + ":");
                // fac asa pentru a printa tot timpul sortat crescator dupa maturitate
                Set<Maturity> maturities = InterestRateConfig.depositInterestRate.get(currency).keySet();
                Maturity[] sortedMaturities = maturities.toArray(new Maturity[maturities.size()]);
                Arrays.sort(sortedMaturities);
                for (Maturity maturity : sortedMaturities)
                    System.out.println("\t\t * " + maturity.getMaturity() +
                            " months: " + InterestRateConfig.depositInterestRate.get(currency).get(maturity) +
                            "%");
            }
        } else
            System.out.println("\nThe interest rates for deposits are not yet set!");
    }

    public static void setDepositInterestRate(Currency currency, int maturity, double interestRate) {
        InterestRateConfig.validateUserInputs(interestRate, new Maturity(maturity));

        if (!InterestRateConfig.depositInterestRate.containsKey(currency))
            InterestRateConfig.depositInterestRate.put(currency, new HashMap<Maturity, Double>());

        InterestRateConfig.depositInterestRate.get(currency).put(new Maturity(maturity), Double.valueOf(interestRate));
    }

    public static void setDepositInterestRate(Currency currency, int[] maturity, double[] interestRate) {
        try {
            if (maturity.length != interestRate.length)
                throw new IllegalArgumentException("Error: the maturity and interesetRate arrays should have the same number of elements!");
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        for (int i = 0; i < maturity.length; ++i)
            InterestRateConfig.setDepositInterestRate(currency, maturity[i], interestRate[i]);
    }

    public static Double getDepositInterestRate(Currency currency, int maturity) {
        if (!InterestRateConfig.depositInterestRate.containsKey(currency))
            return null;

        return InterestRateConfig.depositInterestRate.get(currency).get(new Maturity(maturity));
    }

    public static int[] getAllowedMaturitiesDeposit() {
        return Arrays.copyOf(InterestRateConfig.allowedMaturitiesDeposit,
                InterestRateConfig.allowedMaturitiesDeposit.length);
    }
}
