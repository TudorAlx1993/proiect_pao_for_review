
package customers;

import address.Address;
import audit.AuditService;
import audit.UserType;
import bank.Bank;
import configs.*;
import currency.Currency;
import exceptions.BlockedMailDomainException;
import exceptions.InvalidPhoneNumberException;
import exceptions.WeakPasswordException;
import io.Database;
import products.*;
import services.ExchangeRateService;
import transaction.TransactionDetail;
import transaction.TransactionType;
import utils.Hash;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Customer implements Comparable<Customer>, CustomerOperations {
    private static int noOfCustomers;

    // nu le fac protected
    // folosesc getters in subclase cand am nevoie de ele
    private String hashOfPassword;
    private String phoneNumber;
    private String emailAddress;
    private Address address;
    private final List<Product> products;

    static {
        Customer.noOfCustomers = 0;
    }

    {
        this.products = new ArrayList<>();
    }

    @Override
    public int compareTo(Customer customer) {
        return customer.products.size() - this.products.size();
    }

    protected Customer(String password, String phoneNumber, String emailAddress, Address address, boolean readFromCsvFileOrDatabase) {
        try {
            if (!readFromCsvFileOrDatabase)
                this.checkPasswordRequirments(password);
            this.checkMailDomain(emailAddress);
            this.checkPhoneNumber(phoneNumber);
        } catch (WeakPasswordException |
                BlockedMailDomainException |
                InvalidPhoneNumberException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.address = address;

        if (!readFromCsvFileOrDatabase) {
            Customer.noOfCustomers += 1;
            this.addCurrentAccount("RON");
            this.hashOfPassword = Hash.computeHashOfString(password, CustomerConfig.getHashAlgorithm());
        } else
            this.hashOfPassword = password;

    }

    public abstract String getCustomerName();

    public abstract String getCustomerUniqueID();

    public abstract LocalDate getBirthDay();

    public abstract String getUniqueID();

    public int getAge() {
        LocalDate currentDate = LocalDate.now();
        LocalDate birthDate = this.getBirthDay();

        return Period.between(birthDate, currentDate).getYears();
    }

    @Override
    public void addCurrentAccount(String currencyCode) {
        CurrentAccount currentAccount = new CurrentAccount(new Currency(currencyCode));
        this.products.add(currentAccount);

        // do not log to audit files the creation of the first current account
        // when a customer (Individual or Company) is created, a current account in RON is added by default by a call to the super in the constructor
        // when super is called, the customer name is NULL
        // without the below if, null will appear within the log files as the customer name
        // also the default current account will be saved to database by a call in another script, where the customer id is already set
        if (this.getCustomerUniqueID() != null) {
            Database.saveNewProduct(currentAccount, this);
            AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " created a new current account in " + currencyCode + " with IBAN " + currentAccount.getIBAN());
        }
    }

    private CurrentAccount getCurrentAccount(String iban) {
        for (Product product : this.products)
            if (product instanceof CurrentAccount)
                if (((CurrentAccount) product).getIBAN().equals(iban))
                    return (CurrentAccount) product;

        return null;
    }

    private int noCurrentAccounts(Currency currency) {
        int count = 0;
        for (Product product : this.products)
            if (product instanceof CurrentAccount)
                if (product.getCurrency().equals(currency))
                    count += 1;

        return count;
    }

    public int countDepositsAssociatedToCurrentAccount(CurrentAccount currentAccount) {
        int count = 0;
        for (Product product : this.products)
            if (product instanceof Deposit)
                if (((Deposit) product).getAssociatedCurrentAccount().equals(currentAccount))
                    count += 1;

        return count;
    }

    public int countLoansAssociatedToCurrentAccount(CurrentAccount currentAccount) {
        int count = 0;
        for (Product product : this.products)
            if (product instanceof Loan)
                if (((Loan) product).getCurrentAccount().equals(currentAccount))
                    count += 1;

        return count;
    }

    @Override
    public void deleteCurrentAccount(CurrentAccount currentAccount) {
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this current account is not listed among your products).");
            return;
        }

        // every customers has at least a current account in RON
        // this cannot be deleted
        if (currentAccount.getCurrency().equals(new Currency("RON")) &&
                this.noCurrentAccounts(new Currency("RON")) == 1) {
            System.out.println("Bank message: operation not completed (any customer must have a current account in RON).");
            return;
        }

        if (this.countDepositsAssociatedToCurrentAccount(currentAccount) > 0) {
            System.out.println("Bank message: operation not completed (there are deposits associated with this current acount).");
            return;
        }

        if (this.countLoansAssociatedToCurrentAccount(currentAccount) > 0) {
            System.out.println("Bank message: operation not completed (there are loans associated with this current account).");
            return;
        }

        if (currentAccount.getAmount() > 0) {
            System.out.println("Bank message: operation not completed (the balance of this current account is not zero).");
            return;
        }

        // detete all the associated debit cards with the current account
        this.deleteDebitCard(currentAccount, false);

        String iban = currentAccount.getIBAN();
        this.products.remove(currentAccount);
        Database.deleteProduct(ProductType.CURRENT_ACCOUNT, iban);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " deleted the current account with IBAN " + iban);
    }

    private boolean doesDebitCardExists(CurrentAccount currentAccount) {
        for (Product product : this.products)
            if (product instanceof DebitCard)
                if (((DebitCard) product).getCurrentAcount().equals(currentAccount))
                    return true;

        return false;
    }

    @Override
    public void createDebitCard(CurrentAccount currentAccount,
                                String pin,
                                String networkProcessorName) {
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this current account is not listed among your products).");
            return;
        }

        // the database was designed to allow multiple debit cards on a current account
        // thus, I comment the below lines
        //if (this.doesDebitCardExists(currentAccount)) {
        //    System.out.println("Bank message: operation not completed (there is already a debit card associated to this current account).");
        //    return;
        //}

        DebitCard card = new DebitCard(currentAccount, pin, this.getCustomerName(), networkProcessorName);
        this.products.add(card);

        // some APIs to create and deliver the card
        // external API to inform the network processor that this card is created
        // InternalApi.createDebitCard(card);
        // InternalApi.deliverCard(card,this.getAddress());
        // ExternalApi.activateCard(card);

        Database.saveNewProduct(card, this);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " requested a debit card for the current account with IBAN " + currentAccount.getIBAN());
    }

    private DebitCard getDebitCard(CurrentAccount currentAccount) {
        DebitCard debitCard = null;
        for (Product product : this.products)
            if (product instanceof DebitCard)
                if (((DebitCard) product).getCurrentAcount().getIBAN().equals(currentAccount.getIBAN())) {
                    debitCard = (DebitCard) product;
                    break;
                }
        return debitCard;
    }

    @Override
    public void deleteDebitCard(CurrentAccount currentAccount, boolean printMessage) {
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this current account is not listed among your products).");
            return;
        }

        DebitCard debitCard = this.getDebitCard(currentAccount);

        if (debitCard == null) {
            System.out.println("Bank message: operation not completed (there are not debit cards associated to this current account).");
            return;
        }

        // external API to inform the network processor that this card is invalid
        // ExternalApi.cancelCard((DebitCard) product);

        this.products.remove(debitCard);
        Database.deleteProduct(ProductType.DEBIT_CARD, debitCard.getCardId());
        if (printMessage)
            AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " canceled the debit card associated with " + currentAccount.getIBAN());
    }

    @Override
    public void showMyProducts() {
        System.out.println(this.getCustomerName() + "'s products:");
        for (Product product : this.products)
            System.out.println(product.toString());

        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " viewed its products");
    }

    @Override
    public void transferMoneyToCurrentAccount(CurrentAccount currentAccount, double amount) {
        // this function is only used to make sure than a customer has money
        // to its current account for any kind of banking operation
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (the specified current account is not listed among your products).");
            return;
        }

        if (amount <= 0) {
            System.out.println("Bank message: operation not completed (the amount cannot be negative).");
            return;
        }

        currentAccount.makeTransaction(amount, TransactionType.CREDIT, TransactionDetail.TRANSFER);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " received " + amount + currentAccount.getCurrency().getCurrencyCode() + " in the current account with IBAN " + currentAccount.getIBAN());
    }

    @Override
    public void createDeposit(CurrentAccount currentAccount, double depositAmount, int maturity) {
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (the specified current account is not listed among your products).");
            return;
        }

        if (!currentAccount.checkAmountForTransaction(depositAmount)) {
            System.out.println("Bank message: operation not completed (insufficient funds).");
            return;
        }

        Bank.getBank().createDeposit(this, currentAccount, depositAmount, maturity);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " created a deposit with an amount of " + depositAmount + currentAccount.getCurrency().getCurrencyCode() + " and a maturity of " + maturity + " months");
    }

    @Override
    public void liquidateDepositBeforeMaturity(Deposit deposit) {
        if (!this.checkProduct(deposit.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this deposit is not listed among your products).");
            return;
        }

        double amount = deposit.getDepositAmount();
        Bank.getBank().modifyLiquidity(deposit.getCurrency(), -amount);
        String currencyCode = deposit.getCurrency().getCurrencyCode();
        this.products.remove(deposit);
        deposit.getAssociatedCurrentAccount().makeTransaction(
                amount,
                TransactionType.CREDIT,
                TransactionDetail.LIQUIDATE_DEPOSIT);

        Database.deleteProduct(ProductType.DEPOSIT, deposit.getDepositId());
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " liquidated before maturity a deposit of " + amount + currencyCode);
    }

    @Override
    public void generateCurrentAccountStatement(CurrentAccount currentAccount) {
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this current account is not listed among your products).");
            return;
        }

        currentAccount.generateStatement();
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " generated a statement for the current account with IBAN " + currentAccount.getIBAN());
    }

    @Override
    public void showMyCurrentAccounts() {
        this.showProducts(ProductType.CURRENT_ACCOUNT);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " viewed its current accounts");
    }

    @Override
    public void showMyLoans() {
        this.showProducts(ProductType.LOAN);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " viewed its loans");
    }

    @Override
    public void showMyDeposits() {
        this.showProducts(ProductType.DEPOSIT);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " viewed its deposits");
    }

    @Override
    public void showMyDebitCards() {
        this.showProducts(ProductType.DEBIT_CARD);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " viewed its debit cards");
    }

    @Override
    public void withdrawMoneyFromATM(DebitCard debitCard, String pin, double amount) {
        if (!debitCard.validatePin(pin)) {
            System.out.println("Bank message: operation not completed (invalid PIN).");
            return;
        }

        if (debitCard.isCardExpired()) {
            System.out.println("Bank message: operation not completed (card expired).");
            return;
        }

        double fee = amount * FeesConfig.getAtmWitdrawFeePercent() / 100;
        if (!debitCard.getCurrentAcount().checkAmountForTransaction(amount + fee)) {
            System.out.println("Bank message: operation not completed (insufficient funds).");
            return;
        }

        debitCard.getCurrentAcount().makeTransaction(
                amount,
                TransactionType.DEBIT,
                TransactionDetail.WITHDRAW_MONEY_FROM_ATM
        );
        debitCard.getCurrentAcount().makeTransaction(
                fee,
                TransactionType.DEBIT,
                TransactionDetail.WITHDRAW_FEE_FROM_ATM
        );
        Bank.getBank().modifyLiquidity(debitCard.getCurrency(), fee);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " withdrawn " + amount + debitCard.getCurrentAcount().getCurrency().getCurrencyCode() + " from ATM using the debit card associated to " + debitCard.getCurrentAcount().getIBAN());
    }

    @Override
    public void updateDebitCardPIN(DebitCard debitCard, String oldPIN, String newPIN) {
        if (!this.checkProduct(debitCard.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this debit card is not listed among your products).");
            return;
        }

        if (!debitCard.validatePin(oldPIN)) {
            System.out.println("Bank message: operation not completed (invalid PIN).");
            return;
        }

        debitCard.changePin(oldPIN, newPIN);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " updated the PIN of the debit card associated with " + debitCard.getCurrentAcount().getIBAN());
    }

    @Override
    public void makePayment(CurrentAccount currentAccount,
                            double amount,
                            String destinationIBAN) {
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this current acount is not listed among your products).");
            return;
        }

        double fee;
        TransactionDetail transactionDetail;

        if (destinationIBAN.contains("RO09FBOR")) {
            // internal payment
            transactionDetail = TransactionDetail.INTERNAL_PAYMENT_FEE;
            fee = amount * FeesConfig.getInternalPaymentFeePercent() / 100;

            if (!currentAccount.checkAmountForTransaction(amount + fee)) {
                System.out.println("Bank message: operation not completed (insufficient funds).");
                return;
            }

            // instruct the bank to transfer money to the destination iban
            boolean statusCode = Bank.getBank().addMoneyToCurrentAccount(amount,
                    currentAccount.getCurrency(),
                    destinationIBAN,
                    TransactionDetail.TRANSFER,
                    SystemDate.getDate());
            if (!statusCode) {
                System.out.println("Bank message: operation not completed (invalid destination IBAN or currency mismatch).");
                return;
            }

        } else {
            transactionDetail = TransactionDetail.EXTERNAL_PAYMENT_FEE;
            // external payment
            // call an API to verify te validity of te IBAN
            //if(!IbanVerifier.check(destinationIBAN))
            //    System.out.println("Bank message: operation not completed (invalid destination IBAN)."){
            //    return;
            //}

            fee = amount * FeesConfig.getExternalPaymentFeePercent() / 100;

            if (!currentAccount.checkAmountForTransaction(amount + fee)) {
                System.out.println("Bank message: operation not completed (insufficient funds).");
                return;
            }

            // call an internal API to sent the money to another bank
            // InternalAPI.sendMoneyToAnotherBank(amount,destinationIBAN);
        }

        currentAccount.makeTransaction(
                amount,
                TransactionType.DEBIT,
                TransactionDetail.PAYMENT
        );
        currentAccount.makeTransaction(
                fee,
                TransactionType.DEBIT,
                transactionDetail
        );
        Bank.getBank().modifyLiquidity(currentAccount.getCurrency(), fee);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " sent " + amount + currentAccount.getCurrency().getCurrencyCode() + " from " + currentAccount.getIBAN() + " to " + destinationIBAN);
    }

    @Override
    public void showCurrentInterestRates() {
        InterestRateConfig.showInterestRates();
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " viewed the available interest rates for deposit and loans");

    }

    @Override
    public void applyForLoan(CurrentAccount currentAccount,
                             double requestedAmount,
                             int maturity,
                             double incomeOrTurnover) {
        if (!this.checkProduct(currentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (this current account is not listed among your products).");
            return;
        }

        // call ANAF API cu verify the income or turnover
        //if(!AnafAPI.checkincomeOrTurnove(this.getUniqueID(),incomeOrTurnover)){
        //    System.out.println("Bank message: operation not completed (the income or turnover could not be verified by ANAF).");
        //    return;
        //}

        double currentTotalDebt = this.getTotalDebt();
        // call CRC (Centrala Riscului de Credit)
        // to ask for debts on other banks
        //currentTotalDebt+=CrcApi.getTotalDebt(this.getUniqueID(),includeDebtsToCallerBank=false);

        double debtRatio = (currentTotalDebt + requestedAmount) / incomeOrTurnover;
        if (!this.checkDebtRatio(debtRatio)) {
            System.out.println("Operation not completed (the debt ratio is larger than our requirments).");
            return;
        }

        // call the CRC to check if the customer defaulted on a previous loan
        //if(CrcApi.defaultedOnPreviousLoan(this.getUniqueID())){
        //    System.out.println("Operation not completed (the bank does not approve new loans if the customer defaulted on previous ones).");
        //    return;
        //}

        Bank.getBank().createLoan(this, currentAccount, requestedAmount, maturity);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " was granted a loan of " + requestedAmount + currentAccount.getCurrency().getCurrencyCode() + " with a maturity of " + maturity + " months");
    }

    @Override
    public void askForExchangeRates() {
        ExchangeRateService.showAvailableExchangeRates();
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " viewed the exchange rates");
    }

    @Override
    public void performCurrencyExchange(CurrentAccount fromCurrentAccount,
                                        CurrentAccount toCurrentAccount,
                                        double amountPaid) {
        if (!this.checkProduct(fromCurrentAccount.getProductUniqueId()) ||
                !this.checkProduct(toCurrentAccount.getProductUniqueId())) {
            System.out.println("Bank message: operation not completed (you do not own the current acconts).");
            return;
        }

        if (!fromCurrentAccount.checkAmountForTransaction(amountPaid)) {
            System.out.println("Bank message: operation not completed (insufficient funds).");
            return;
        }

        ExchangeRateService exchangeRateService = new ExchangeRateService(fromCurrentAccount.getCurrency(), toCurrentAccount.getCurrency());
        double amountReceived = exchangeRateService.getAmountInPairedCurrency(amountPaid);

        // instruct the bank to perform the transaction
        Bank.getBank().performExchangeRateService(
                fromCurrentAccount,
                amountPaid,
                toCurrentAccount,
                amountReceived);
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " exchanged " + amountPaid + fromCurrentAccount.getCurrency().getCurrencyCode() + " for " + toCurrentAccount.getCurrency().getCurrencyCode());
    }

    private boolean checkDebtRatio(double debtRatio) {
        if (this.getCustomerType() == CustomerType.COMPANY)
            return debtRatio <= LoanConfig.getMaxThresholdDebtRatioCompany();
        else if (this.getCustomerType() == CustomerType.INDIVIDUAL)
            return debtRatio <= LoanConfig.getMaxThresholdDebtRatioIndividual();
        else
            return false;
    }

    private double getTotalDebt() {
        double debt = 0;
        for (Product product : this.products)
            if (product instanceof Loan)
                debt += ((Loan) product).getLoanCurrentAmount();

        return debt;
    }


    private boolean checkProduct(String uniqueID) {
        // this function is used to check that a customer does not use other customers product
        for (Product product : this.products)
            if (product.getProductUniqueId().equals(uniqueID))
                return true;

        return false;
    }

    private void showProducts(ProductType productType) {
        int count = 0;
        for (Product product : this.products)
            if (product.getProductType() == productType) {
                System.out.println(product.toString());
                count += 1;
            }

        if (count == 0) {
            System.out.println("Bank message: you do not have products with category=" + productType.toString() + ".");
        }
    }


    // utilizez metode private fiidnca utilizarea lor are loc doar in aceasta clasa
    private void checkPasswordRequirments(String password) throws WeakPasswordException {
        boolean throwException = false;
        // comentez liniile de mai jos
        // findnca nu vreau sa generez parole complexe cand testez clasa
        //if (!password.matches(CustomerConfig.getPasswordPattern()))
        //    throwException = true;

        if (throwException)
            throw new WeakPasswordException();
    }

    private void checkMailDomain(String emailAddress) throws BlockedMailDomainException {
        boolean throwException = false;

        for (String emailDomain : MailCommunication.getBlockedMailDomains())
            if (emailAddress.toLowerCase().contains(emailDomain.toLowerCase())) {
                throwException = true;
                break;
            }

        // here we call an API which verified if the provided mail is valid
        // if(!SomeMailApi.checkEmailAddress(emailAddress))
        //        throwException=true;

        if (throwException)
            throw new BlockedMailDomainException("Error: the bank does not communicate with its clients on " + emailAddress.toLowerCase() + " mail domain!");
    }

    private void checkPhoneNumber(String phoneNumber) throws InvalidPhoneNumberException {
        boolean throwException = false;

        // some external API to verify if this phone number is valid
        // if(!SomePhoneNumberApi.checkPhoneNumber(phoneNumber))
        //          throwException=true;

        if (throwException)
            throw new InvalidPhoneNumberException();
    }

    public boolean verifyCustomerIdentity(String password) {
        return this.hashOfPassword.equals(Hash.computeHashOfString(password, CustomerConfig.getHashAlgorithm()));
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        if (!this.verifyCustomerIdentity(oldPassword)) {
            System.out.println("Bank message: incorrect password!");
            return;
        }

        try {
            this.checkPasswordRequirments(newPassword);
        } catch (WeakPasswordException exception) {
            System.out.println("Bank message: operation not completed (weak password).");
            return;
        }

        this.hashOfPassword = Hash.computeHashOfString(newPassword, CustomerConfig.getHashAlgorithm());
        AuditService.addLoggingData(UserType.CUSTOMER, this.getCustomerName() + " updated the password");
    }

    public static int getNoOfCustomers() {
        return Customer.noOfCustomers;
    }

    public static void setNoOfCustomers(int noOfCustomers) {
        Customer.noOfCustomers = noOfCustomers;
    }

    public String getHashOfPassword() {
        return this.hashOfPassword;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        try {
            this.checkPhoneNumber(phoneNumber);
        } catch (InvalidPhoneNumberException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        try {
            this.checkMailDomain(emailAddress);
        } catch (BlockedMailDomainException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.emailAddress = emailAddress;
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "\t* phone number: " + this.phoneNumber + "\n" +
                "\t* email address: " + this.emailAddress + "\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hashOfPassword,
                this.phoneNumber,
                this.emailAddress,
                this.address,
                this.products.hashCode());
    }

    public List<Product> getProducts() {
        return this.products;
    }

    public abstract CustomerType getCustomerType();

    private static void setNoCustomers(int noOfCustomers) {
        Customer.noOfCustomers = noOfCustomers;
    }

    public static List<String> getHeaderForCsvFile() {
        return Stream.of("customer_type",
                        "customer_id",
                        "customer_name",
                        "birth_date",
                        "hash_of_password",
                        "phone_number",
                        "email_address",
                        "address_country",
                        "address_city",
                        "address_zip_code",
                        "address_street_name",
                        "address_street_number",
                        "address_additional_info")
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    public List<String> getDataForCsvWriting() {
        return Arrays.asList(this.hashOfPassword,
                this.phoneNumber,
                this.emailAddress,
                this.address.getCountry(),
                this.address.getCity(),
                this.address.getZipCode(),
                this.address.getStreetName(),
                String.valueOf(this.address.getStreetNumber()),
                this.address.getAdditionalInfo().equals("") ? "NA" : this.address.getAdditionalInfo());
    }

    protected void saveCustomerToDatabase(Customer customer) {
        Database.saveNewCustomer(customer);
    }
}