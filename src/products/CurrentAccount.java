package products;

import configs.IBANConfig;
import configs.SystemDate;
import currency.Currency;
import transaction.TransactionDetail;
import transaction.TransactionLogger;
import transaction.TransactionType;
import utils.AmountFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CurrentAccount extends Product {
    private static int noCurrentAccounts;

    private double amount;
    private final String iban;
    private final List<TransactionLogger> transactions;

    static {
        CurrentAccount.noCurrentAccounts = 0;
    }

    {
        this.amount = 0;
        this.iban = this.generateIBAN();
    }

    public CurrentAccount(Currency currency, LocalDate openDate) {
        super(currency, openDate);

        CurrentAccount.noCurrentAccounts += 1;

        this.transactions = new ArrayList<TransactionLogger>();
    }

    public CurrentAccount(Currency currency) {
        // current date
        this(currency, SystemDate.getDate());
    }

    public void addTransaction(TransactionLogger transactionLogger) {
        this.transactions.add(transactionLogger);
    }

    private String generateIBAN() {
        StringBuilder iban = new StringBuilder();

        // country code
        iban.append("RO09");
        // bank code
        iban.append("FBOR");

        //complete the rest of the IBAN using random numbers
        final int noRandomNumbers = IBANConfig.getIbanLength() - iban.length();
        final int maxNumber = 10;
        for (int i = 0; i < noRandomNumbers; ++i) {
            int randomNumber = this.generateRandomNumber(maxNumber);
            iban.append(randomNumber);
        }

        return iban.toString();
    }

    public boolean checkAmountForTransaction(double amount) {
        if ((this.amount - amount) < 0)
            return false;

        return true;
    }

    public void makeTransaction(double amount,
                                TransactionType transactionType,
                                TransactionDetail transactionDetail,
                                LocalDate date) {
        // transactionType is DEBIT or CREDIT only
        if (transactionType == TransactionType.CREDIT)
            this.amount += amount;
        else
            this.amount -= amount;

        String details = this.getTransactionDetailAsString(transactionDetail);

        TransactionLogger transactionLogger = new TransactionLogger(transactionType, amount, details, date);
        this.transactions.add(transactionLogger);
    }

    public void makeTransaction(double amount,
                                TransactionType transactionType,
                                TransactionDetail transactionDetail) {
        this.makeTransaction(amount, transactionType, transactionDetail, SystemDate.getDate());
    }

    private String getTransactionDetailAsString(TransactionDetail transactionDetail) {
        switch (transactionDetail) {
            case TRANSFER:
                return "transfer";
            case CREATE_DEPOSIT:
                return "new deposit";
            case LIQUIDATE_DEPOSIT:
                return "liquidate deposit";
            case WITHDRAW_MONEY_FROM_ATM:
                return "money withdraw from ATM";
            case WITHDRAW_FEE_FROM_ATM:
                return "ATM's withdraw fee";
            case INTERNAL_PAYMENT_FEE:
                return "internal payment fee";
            case EXTERNAL_PAYMENT_FEE:
                return "external payment fee";
            case PAYMENT:
                return "payment";
            case LOAN_GRANTING:
                return "loan granting";
            case CURRENCY_EXCHAGE_PAID:
                return "paid in exchage rate service";
            case CURRENCY_EXCHANGE_RECEIVED:
                return "received in exchange rate service";
            case INTEREST_FROM_DEPOSIT:
                return "interest from deposit";
            case PAYMENT_FOR_LOAN:
                return "payment for loan";
            case INTEREST_FOR_LOAN:
                return "interest for loan";
            default:
                return "other transaction";
        }
    }

    public double getAmount() {
        return this.amount;
    }

    public String getIBAN() {
        return this.iban;
    }

    @Override
    public String getProductUniqueId() {
        return this.getIBAN();
    }

    public static int getNoCurrentAccounts() {
        return CurrentAccount.noCurrentAccounts;
    }

    public static void setNoCurrentAccounts(int noCurrentAccounts){
        CurrentAccount.noCurrentAccounts=noCurrentAccounts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                this.iban,
                this.amount,
                this.transactions.hashCode());
    }

    @Override
    public String toString() {
        return "Current account details:\n" +
                "\t* IBAN: " + this.iban + "\n" +
                "\t* amount: " + AmountFormatter.format(this.amount) + "\n" +
                super.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof CurrentAccount))
            return false;

        CurrentAccount currentAccount = (CurrentAccount) object;
        if (!this.iban.equals(currentAccount.getIBAN()))
            return false;

        return true;
    }

    public void generateStatement() {
        // extras de cont
        // make sure the transactions are show sorted ascending after transaction date
        Collections.sort(this.transactions);
        System.out.println(this.toString());
        System.out.println("TRANSACTION ID\t DATE\t AMOUNT\t TRANSACTION TYPE\t DETAILS");
        for (TransactionLogger transaction : this.transactions)
            System.out.println(transaction.toString());
    }

    @Override
    public ProductType getProductType() {
        return ProductType.CURRENT_ACCOUNT;
    }

    public List<TransactionLogger> getTransactions() {
        return this.transactions;
    }


    @Override
    public List<String> getProductHeaderForCsvFile() {
        List<String> fileHeader = Stream.of("iban",
                        "amount")
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        fileHeader.addAll(super.getProductHeaderForCsvFile());

        return fileHeader;
    }

    @Override
    public List<String> getProductDataForCsvWriting(String customerID) {
        List<String> lineContent = new ArrayList<>();

        lineContent.add(this.iban);
        lineContent.add(String.valueOf(this.amount));
        lineContent.addAll(super.getProductDataForCsvWriting(customerID));

        return lineContent;
    }
}
