package products;

import configs.Codes;
import configs.DepositConfig;
import configs.InterestRateConfig;
import configs.SystemDate;
import exceptions.InvalidInterestRateException;
import utils.AmountFormatter;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.Objects;

public class Deposit extends Product {
    private static int noOfDeposits;

    private final String depositId;
    private final double depositAmount;
    private final double interestRate;
    private final double interest;
    private final LocalDate depositMaturity;
    private final CurrentAccount currentAccount;

    static {
        noOfDeposits = 0;
    }

    public Deposit(CurrentAccount currentAccount,
                   LocalDate openDate,
                   int maturityInMonths,
                   double depositAmount) {
        super(currentAccount.getCurrency(), openDate);

        try {
            this.validateDepositParameters(maturityInMonths, depositAmount);
        } catch (InvalidInterestRateException |
                InvalidParameterException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        Deposit.noOfDeposits += 1;

        this.depositId = this.generateUniqueID(DepositConfig.getDepositUniqueIdLength(), 10);
        this.depositAmount = depositAmount;
        this.interestRate = InterestRateConfig.getDepositInterestRate(this.getCurrency(), maturityInMonths).doubleValue();
        this.depositMaturity = this.getOpenDate().plusMonths(maturityInMonths);
        this.currentAccount = currentAccount;
        this.interest = this.calculateDepositInterest(depositAmount, this.interestRate, maturityInMonths);
    }

    public Deposit(CurrentAccount currentAccount, int maturityInMonths, double depositAmount) {
        this(currentAccount, SystemDate.getDate(), maturityInMonths, depositAmount);
    }

    public boolean doesDepositReachedMaturity() {
        return SystemDate.getDate().compareTo(this.depositMaturity)>=0;
    }

    private double calculateDepositInterest(double depositAmount, double interestRate, int maturityInMonths) {
        final int monthsOnYear = 12;
        interestRate /= 100;

        return depositAmount * interestRate * maturityInMonths / monthsOnYear;
    }

    private void validateDepositParameters(int maturity, double amount) throws InvalidInterestRateException, IllegalArgumentException {
        if (amount <= 0)
            throw new IllegalArgumentException("Error: the deposit's nomival value must the strictly positive!");

        if (InterestRateConfig.getDepositInterestRate(this.getCurrency(), maturity) == null)
            throw new InvalidInterestRateException("Error: the bank does not offer deposits for the requested currency and maturity!");
    }

    public double getDepositAmount() {
        return this.depositAmount;
    }

    public LocalDate getDepositMaturity() {
        return this.depositMaturity;
    }

    public double getInterestRate() {
        return this.interestRate;
    }

    public String getDepositId() {
        return this.depositId;
    }

    @Override
    public String getProductUniqueId() {
        return this.getDepositId();
    }

    public CurrentAccount getAssociatedCurrentAccount() {
        return this.currentAccount;
    }

    public static int getNoOfDeposits() {
        return Deposit.noOfDeposits;
    }

    public double getInterestAtMaturity() {
        return this.interest;
    }

    @Override
    public String toString() {
        return "Deposit summary:\n" +
                "\t* deposit id: " + this.depositId + "\n" +
                "\t* amount: " + AmountFormatter.format(this.depositAmount) + "\n" +
                "\t* currency: " + this.getCurrency().toString() + "\n" +
                "\t* interest rate: " + this.interestRate + "%" + "\n" +
                "\t* intereset at maturity: " + AmountFormatter.format(this.interest) + "\n" +
                "\t* open date: " + this.getOpenDate().toString() + "\n" +
                "\t* maturity date: " + this.depositMaturity.toString() + "\n" +
                "\t* source IBAN: " + this.currentAccount.getIBAN() + "\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                this.depositId,
                this.depositAmount,
                this.interestRate,
                this.interest,
                this.depositMaturity.hashCode(),
                this.currentAccount.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof Deposit))
            return false;

        Deposit deposit = (Deposit) object;
        if (!this.depositId.equals(deposit.depositId))
            return false;

        return true;
    }

    @Override
    public ProductType getProductType() {
        return ProductType.DEPOSIT;
    }
}

