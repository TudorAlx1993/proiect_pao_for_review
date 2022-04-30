package products;

import configs.Codes;
import configs.InterestRateConfig;
import configs.LoanConfig;
import configs.SystemDate;
import currency.Currency;
import customers.CustomerType;
import exceptions.InvalidInterestRateException;
import exceptions.InvalidLoanMaturityException;
import exceptions.NotImplementedCustomerException;
import utils.AmountFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Loan extends Product {
    private static int noOfLoans;

    private final String loanId;
    private double interestRate;
    private final int maturityInMonths;
    private final ArrayList<LocalDate> paymentDates;
    private int indexToNextPaymentDate;
    // payments will come from this account
    private final CurrentAccount currentAccount;
    private final double loanInitialAmount;
    private double loanCurrentAmount;

    static {
        noOfLoans = 0;
    }

    public Loan(CustomerType customerType,
                CurrentAccount currentAccount,
                LocalDate openDate,
                double amount,
                int maturityInMonths) {

        super(currentAccount.getCurrency(), openDate);

        try {
            this.validateInputs(amount, maturityInMonths, currentAccount.getCurrency(), customerType);
        } catch (IllegalArgumentException |
                NotImplementedCustomerException |
                InvalidLoanMaturityException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        Loan.noOfLoans += 1;

        this.loanId = this.generateUniqueID(LoanConfig.getLoanUniqueIdLength(), 10);
        this.interestRate = InterestRateConfig.getLoanInterestRate(currentAccount.getCurrency());
        this.maturityInMonths = maturityInMonths;
        this.paymentDates = this.generatePaymentDates();
        this.indexToNextPaymentDate = 0;
        this.currentAccount = currentAccount;
        this.loanInitialAmount = amount;
        this.loanCurrentAmount = amount;
    }

    public Loan(CustomerType customerType,
                CurrentAccount currentAccount,
                double amount,
                int maturityInMonths) {
        this(customerType, currentAccount, SystemDate.getDate(), amount, maturityInMonths);
    }

    public void checkForUpdatedInterestRate() {
        Double newInterestRate = InterestRateConfig.getLoanInterestRate(this.currentAccount.getCurrency());
        if (newInterestRate != null && this.interestRate != newInterestRate.doubleValue())
            this.interestRate = newInterestRate.doubleValue();
    }


    public double getInterestPaymentAmount() {
        final int monthsInYear = 12;
        double interest = this.loanCurrentAmount * (this.interestRate / 100) / monthsInYear;

        return interest;
    }

    public double getPrincipalPaymentAmount() {
        double principal = this.loanInitialAmount / this.maturityInMonths;

        return principal;
    }

    public LocalDate getNextPaymentDate() {
        return this.paymentDates.get(this.indexToNextPaymentDate);
    }

    private ArrayList<LocalDate> generatePaymentDates() {
        ArrayList<LocalDate> paymentDates = new ArrayList<>(this.maturityInMonths);

        paymentDates.add(this.getOpenDate().plusMonths(1));
        for (int i = 1; i < this.maturityInMonths; ++i)
            paymentDates.add(paymentDates.get(i - 1).plusMonths(1));

        return paymentDates;
    }

    private void validateInputs(double amount,
                                int maturity,
                                Currency currency,
                                CustomerType customerType) throws InvalidLoanMaturityException,
            NotImplementedCustomerException,
            IllegalArgumentException {
        if (amount <= 0.0)
            throw new IllegalArgumentException("Error: the loan nominal value should be strictly positive!");

        boolean illegalMaturity = false;
        switch (customerType) {
            case INDIVIDUAL:
                if (maturity < LoanConfig.getMinLoanMaturityIndividual() ||
                        maturity > LoanConfig.getMaxLoanMaturityIndividual())
                    illegalMaturity = true;
                break;
            case COMPANY:
                if (maturity < LoanConfig.getMinLoanMaturityCompany() ||
                        maturity > LoanConfig.getMaxLoanMaturityCompany())
                    illegalMaturity = true;
                break;
            default:
                throw new NotImplementedCustomerException("Error: customer type not implemented!");
        }

        if (illegalMaturity)
            throw new InvalidLoanMaturityException("Error: the requested maturity for the loan is not allowed!");

        if (InterestRateConfig.getLoanInterestRate(currency) == null)
            throw new InvalidInterestRateException("Error: the interest rate is not set for loans denominated in " +
                    currency.toString() + "!");
    }

    public static int getNoOfLoans() {
        return Loan.noOfLoans;
    }

    public static void setNoOfLoans(int noOfLoans){
        Loan.noOfLoans=noOfLoans;
    }

    public String getLoanId() {
        return this.loanId;
    }

    @Override
    public String getProductUniqueId() {
        return this.getLoanId();
    }

    public double getInterestRate() {
        return this.interestRate;
    }

    public int getMaturityInMonths() {
        return this.maturityInMonths;
    }

    public ArrayList<LocalDate> getPaymentDates() {
        return new ArrayList<>(this.paymentDates);
    }

    public int getIndexToNextPaymentDate() {
        return this.indexToNextPaymentDate;
    }

    public void updateIndexToNextPayment() {
        this.indexToNextPaymentDate += 1;
    }

    public void updatePaymentDatesBecauseOfMissingCurrentPrincipalPayment() {
        for (int i = 0; i < this.paymentDates.size(); ++i)
            this.paymentDates.set(i, this.paymentDates.get(i).plusMonths(1));
    }

    public CurrentAccount getCurrentAccount() {
        return this.currentAccount;
    }

    public double getLoanInitialAmount() {
        return this.loanInitialAmount;
    }

    public double getLoanCurrentAmount() {
        return this.loanCurrentAmount;
    }

    public void decreaseLoanCurrentAmount(double amount) {
        this.loanCurrentAmount -= amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                this.loanId,
                this.interestRate,
                this.maturityInMonths,
                this.paymentDates.hashCode(),
                this.indexToNextPaymentDate,
                this.currentAccount.hashCode(),
                this.loanInitialAmount,
                this.loanCurrentAmount);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof Loan))
            return false;

        Loan loan = (Loan) object;
        if (!this.loanId.equals(loan.loanId))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "Loan summary:\n" +
                "\t* loan id: " + this.loanId + "\n" +
                "\t* initial amount: " + AmountFormatter.format(this.loanInitialAmount) + "\n" +
                "\t* current amount: " + AmountFormatter.format(this.loanCurrentAmount) + "\n" +
                "\t* currency: " + this.getCurrency().toString() + "\n" +
                "\t* origination date: " + this.getOpenDate().toString() + "\n" +
                "\t* maturity date: " + this.paymentDates.get(this.paymentDates.size() - 1).toString() + "\n" +
                "\t* next payment date: " + this.paymentDates.get(this.indexToNextPaymentDate).toString() + "\n" +
                "\t* current interest rate: " + this.interestRate + "%\n";

    }

    @Override
    public ProductType getProductType() {
        return ProductType.LOAN;
    }

    @Override
    public List<String> getProductHeaderForCsvFile() {
        List<String> fileHeader = Stream.of("loan_id",
                        "interest_rate",
                        "maturity_in_months",
                        "index_to_next_payment",
                        "loan_initial_amount",
                        "loan_current_amount",
                        "associated_iban")
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        fileHeader.addAll(super.getProductHeaderForCsvFile());

        return fileHeader;
    }

    @Override
    public List<String> getProductDataForCsvWriting(String customerID) {
        List<String> lineContent = new ArrayList<>();

        lineContent.add(this.loanId);
        lineContent.add(String.valueOf(this.interestRate));
        lineContent.add(String.valueOf(this.maturityInMonths));
        lineContent.add(String.valueOf(this.indexToNextPaymentDate));
        lineContent.add(String.valueOf(this.loanInitialAmount));
        lineContent.add(String.valueOf(this.loanCurrentAmount));
        lineContent.add(this.currentAccount.getIBAN());
        lineContent.addAll(super.getProductDataForCsvWriting(customerID));

        return lineContent;
    }
}