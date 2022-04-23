package bank;

import address.Address;
import configs.ExchangeRatesConfig;
import configs.FeesConfig;
import configs.InterestRateConfig;
import configs.SystemDate;
import currency.Currency;
import customers.Company;
import customers.Customer;
import customers.Individual;
import products.*;
import services.ExchangeRateService;
import transaction.TransactionDetail;
import transaction.TransactionType;
import utils.AmountFormatter;

import java.time.LocalDate;
import java.util.*;

public final class Bank implements BankActions {
    // when I write final String
    // it means that I want to make clear that I do no want to modify the reference
    private final String bankName;
    private final String motto;
    private final int foundingYear;
    private final String phoneNumber;
    private final String emailAddress;
    private final String website;
    // for singleton only
    private static Bank bank;
    // store the bank's liquidity
    private final Map<Currency, Double> liquidity;
    // store the bank's customers
    private final List<Customer> customers;

    private Bank(String bankName,
                 String motto,
                 int foundingYear,
                 String phoneNumber,
                 String emailAddress,
                 String website) {
        this.bankName = bankName;
        this.motto = motto;
        this.foundingYear = foundingYear;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.website = website;
        //initialize the bank's own current accounts
        //those accounts represent the bank capital
        //the bank will use this money to lend to customers
        // customers will deposit money to those accounts
        this.liquidity = new HashMap<Currency, Double>();
        this.customers = new ArrayList<Customer>();

    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
    }

    @Override
    public void showCustomers() {
        System.out.println("\nCustomers details:\n");
        for (Customer customer : this.customers)
            System.out.println(customer.toString());
    }

    @Override
    public void sortCustomersByNoProductsDesc() {
        Collections.sort(this.customers);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void showCustomersSummary() {
        System.out.println("\nCustomers details (summary):");
        System.out.println("\t* total customers (including deleted): " + Customer.getNoOfCustomers());
        System.out.println("\t* total active customers: " + this.customers.size());
    }

    @Override
    public void showSystemDate(){
        System.out.println("System date: "+SystemDate.getDate());
    }

    @Override
    public void addCustomerFromKeyboard() {
        System.out.println("Options:");
        System.out.println("\t* 1) individual");
        System.out.println("\t* 2) company");
        Scanner scanner = new Scanner(System.in);

        int userSelection;
        while (true) {
            System.out.print("Enter your selection: ");
            userSelection = scanner.nextInt();

            if (userSelection == 1 || userSelection == 2)
                break;
            else
                System.out.println("Invalid choice. Please try again!");
        }

        // https://stackoverflow.com/questions/7877529/java-string-scanner-input-does-not-wait-for-info-moves-directly-to-next-stateme?rq=1
        scanner.nextLine();

        System.out.print("Enter your phone number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter your email address: ");
        String emailAddress = scanner.nextLine();
        System.out.print("Enter you password: ");
        String password = scanner.nextLine();
        Address address = Address.readAddressFromKeyboard();

        scanner.nextLine();

        Customer customer = null;

        if (userSelection == 1) {
            System.out.print("Enter your CNP (13 digits): ");
            String cnp = scanner.nextLine();
            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter your last name: ");
            String lastName = scanner.nextLine();

            customer = new Individual(firstName, lastName, cnp, password, phoneNumber, emailAddress, address);
        } else {
            System.out.print("Enter the company name: ");
            String companyName = scanner.nextLine();
            System.out.print("Enter your cui (6 digits): ");
            String cui = scanner.nextLine();
            System.out.print("Enter the day of the establishment date: ");
            int day = scanner.nextInt();
            System.out.print("Enter the month of the establishment date: ");
            int month = scanner.nextInt();
            System.out.print("Enter the year of the establishment date: ");
            int year = scanner.nextInt();

            customer = new Company(companyName, cui, LocalDate.of(year, month, day), password, phoneNumber, emailAddress, address);
        }

        this.customers.add(customer);
    }

    @Override
    public void showProductsSummary() {
        int countCurrentAccounts, countDebitCards, countDeposits, countLoans;
        countCurrentAccounts = countDebitCards = countDeposits = countLoans = 0;

        for (Customer customer : this.customers)
            for (Product product : customer.getProducts()) {
                if (product instanceof CurrentAccount)
                    countCurrentAccounts += 1;
                else if (product instanceof DebitCard)
                    countDebitCards += 1;
                else if (product instanceof Deposit)
                    countDeposits += 1;
                else if (product instanceof Loan)
                    countLoans += 1;
            }

        System.out.println("There are " +
                (countCurrentAccounts + countDebitCards + countDeposits + countLoans) +
                " active products:");
        System.out.println("\t* no of current accounts: " + countCurrentAccounts);
        System.out.println("\t* no of debit cards: " + countDebitCards);
        System.out.println("\t* no of deposits: " + countDeposits);
        System.out.println("\t* no of loans: " + countLoans);
    }


    public void modifyLiquidity(Currency currency, double amount) {
        currency = new Currency(currency);
        if (this.liquidity.containsKey(currency))
            this.liquidity.put(currency, this.liquidity.get(currency) + Double.valueOf(amount));
        else
            this.liquidity.put(currency, Double.valueOf(amount));
    }

    @Override
    public void showLiquiditySummary() {
        System.out.println("Summary of the bank's liquidity");
        System.out.println("Balance:");
        for (Currency key : this.liquidity.keySet()) {
            double amount = this.liquidity.get(key).doubleValue();
            String message = "\t* " + AmountFormatter.format(amount) + " " + key.getCurrencyCode();
            System.out.println(message);
        }
    }

    @Override
    public void setExchangeRate(Currency currency, double exchangeRate) {
        if (exchangeRate <= 0) {
            System.out.println("Bank message: operation not completed (the exchange rate must be positive).");
            return;
        }

        ExchangeRatesConfig.setReferenceExchangeRateOfCurrencyPerRON(currency, exchangeRate);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void setAskSpreadPercent(double askSpreadPercent) {
        if (askSpreadPercent < ExchangeRatesConfig.getMinSpreadPercent() ||
                askSpreadPercent > ExchangeRatesConfig.getMaxSpreadPercent()) {
            System.out.println("Bank message: operation not completed (spread must be between configured min and max spread).");
            return;
        }

        ExchangeRatesConfig.setAskSpreadPercent(askSpreadPercent);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void setBidSpreadPercent(double bidSpreadPercent) {
        if (bidSpreadPercent < ExchangeRatesConfig.getMinSpreadPercent() ||
                bidSpreadPercent > ExchangeRatesConfig.getMaxSpreadPercent()) {
            System.out.println("Bank message: operation not completed (spread must be between the configured min and max spread).");
            return;
        }

        ExchangeRatesConfig.setBidSpreadPercent(bidSpreadPercent);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void setLoanInterestRate(Currency currency, double interestRate) {
        if (interestRate <= 0.0 || interestRate >= 100.0) {
            System.out.println("Bank message: operation not completed (intereset rate should be between 0 and 100).");
            return;
        }

        InterestRateConfig.setLoanInterestRate(currency, interestRate);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void showFees() {
        System.out.println("Current fees:");
        System.out.println("\t* internal payment fee: " + FeesConfig.getInternalPaymentFeePercent() + "%");
        System.out.println("\t* external payment fee: " + FeesConfig.getExternalPaymentFeePercent() + "%");
        System.out.println("\t* atm withdrawn fee: " + FeesConfig.getAtmWitdrawFeePercent() + "%");
    }

    @Override
    public void showExchangeRates(){
        ExchangeRateService.showAvailableExchangeRates();
    }

    @Override
    public void setDepositInterestRate(Currency currency, int maturity, double interestRate) {
        if (interestRate <= 0.0 || interestRate >= 100.0) {
            System.out.println("Bank message: operation not completed (intereset rate should be between 0 and 100).");
            return;
        }

        if (!Arrays.stream(InterestRateConfig.getAllowedMaturitiesDeposit()).anyMatch(allowedMaturity -> allowedMaturity == maturity)) {
            System.out.println("Bank message: operation not completed (allowed maturities are: " +
                    Arrays.toString(InterestRateConfig.getAllowedMaturitiesDeposit()) +
                    " months).");
            return;
        }

        InterestRateConfig.setDepositInterestRate(currency, maturity, interestRate);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void setInternalPaymentFee(double internalPaymentFeePercent) {
        double minFeePercent = FeesConfig.getMinFeePercent();
        double maxFeePercent = FeesConfig.getMaxFeePercent();
        if (internalPaymentFeePercent < minFeePercent ||
                internalPaymentFeePercent > maxFeePercent) {
            System.out.println("Bank message: operation not completed (the fee must be between " +
                    minFeePercent +
                    "% and " +
                    maxFeePercent +
                    "%).");
            return;
        }

        FeesConfig.setInternalPaymentFeePercent(internalPaymentFeePercent);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void setExternalPaymentFee(double externalPaymentFeePercent) {
        double minFeePercent = FeesConfig.getMinFeePercent();
        double maxFeePercent = FeesConfig.getMaxFeePercent();
        if (externalPaymentFeePercent < minFeePercent ||
                externalPaymentFeePercent > maxFeePercent) {
            System.out.println("Bank message: operation not completed (the fee must be between " +
                    minFeePercent +
                    "% and " +
                    maxFeePercent +
                    "%).");
            return;
        }

        FeesConfig.setExternalPaymentFeePercent(externalPaymentFeePercent);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void setAtmWithdrawFee(double atmWithdrawFeePercent) {
        double minFeePercent = FeesConfig.getMinFeePercent();
        double maxFeePercent = FeesConfig.getMaxFeePercent();
        if (atmWithdrawFeePercent < minFeePercent ||
                atmWithdrawFeePercent > maxFeePercent) {
            System.out.println("Bank message: operation not completed (the fee must be between " +
                    minFeePercent +
                    "% and " +
                    maxFeePercent +
                    "%).");
            return;
        }

        FeesConfig.setAtmWitdrawFeePercent(atmWithdrawFeePercent);
        System.out.println("Bank message: operation completed.");
    }

    @Override
    public void setSystemDate(int day, int month, int year) {
        // functia asta o utilizam pentru a verifica daca sunt deposite care au ajuns la scadenta
        // sau daca banca un client trebuie sa plateasca dobanda + principalul pentru un credit
        LocalDate newDate = LocalDate.of(year, month, day);
        if (newDate.compareTo(LocalDate.now()) < 0) {
            System.out.println("Bank message: operation not completed (the system date cannot be before the real date).");
            return;
        }

        SystemDate.setDate(newDate);
        System.out.println("Bank message: operation completed.");

        this.checkForDepositsThatReachedMaturity();
        this.checkForLoansThatReachedPaymentDay();
    }

    // folosesc functii private fiindca nu vreau sa fie accesibile in afara clasei
    private void checkForDepositsThatReachedMaturity() {
        for (Customer customer : this.customers) {
            List<Integer> productIndexes = new ArrayList<Integer>();
            int productIndex = -1;
            for (Product product : customer.getProducts()) {
                productIndex += 1;
                if (product instanceof Deposit) {
                    Deposit deposit = (Deposit) product;
                    if (deposit.doesDepositReachedMaturity()) {
                        productIndexes.add(Integer.valueOf(productIndex));

                        double principal = deposit.getDepositAmount();
                        double interest = deposit.getInterestAtMaturity();
                        Currency currency = deposit.getCurrency();
                        LocalDate depositMaturity = deposit.getDepositMaturity();

                        this.modifyLiquidity(currency, -(principal + interest));

                        this.addMoneyToCurrentAccount(principal,
                                currency,
                                deposit.getAssociatedCurrentAccount().getIBAN(),
                                TransactionDetail.LIQUIDATE_DEPOSIT,
                                depositMaturity);
                        this.addMoneyToCurrentAccount(interest,
                                currency,
                                deposit.getAssociatedCurrentAccount().getIBAN(),
                                TransactionDetail.INTEREST_FROM_DEPOSIT,
                                depositMaturity);

                        // nu putem face products.remove(deposit) atunci cand lucram cu iterators
                    }
                }
            }
            // delete the products that are deposits and reached maturity
            int count = 0;
            // count++ adica update de index dupa fiecare stergere
            //System.out.println(productIndexes.size());
            for (Integer indexOfProductToDelete : productIndexes)
                customer.getProducts().remove(indexOfProductToDelete.intValue() - (count++));
        }
    }

    private void checkForLoansThatReachedPaymentDay() {
        // aici este mai greu de implementat logica
        // mi-am dat seama la sfarsit
        // si nu prea mai am timp
        // ar trebui sa iau in calcul si platite de dobanzi si principal (care sunt lunare) intre data acordarii creditului
        // si data curenta a sistemului
        // plus situatia in care clientul nu are bani in contul curenta pentru plata
        // o sa scriu aceasta functie cand o sa ma apuc de a doua parte a proiectului
    }

    public static Bank getBank(String bankName,
                               String motto,
                               int foundingYear,
                               String phoneNumber,
                               String emailAddress,
                               String website) {
        if (Bank.bank == null)
            Bank.bank = new Bank(bankName, motto, foundingYear,
                    phoneNumber, emailAddress, website);

        return Bank.bank;
    }

    public static Bank getBank() {
        return Bank.bank;
    }

    public String getBankName() {
        return this.bankName;
    }

    public String getMotto() {
        return this.motto;
    }

    public int getFoundingYear() {
        return this.foundingYear;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public String getWebsite() {
        return this.website;
    }

    @Override
    public String toString() {
        String bankDescription = "Welcome to " + this.bankName + "\n" +
                "Motto: " + this.motto + "\n" +
                "Founding year: " + this.foundingYear + "\n" +
                "Phone number: " + this.phoneNumber + "\n" +
                "Email address: " + this.emailAddress + "\n" +
                "Website: " + this.website + "\n";

        return bankDescription;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bankName,
                this.motto,
                this.foundingYear,
                this.phoneNumber,
                this.emailAddress,
                this.website,
                this.liquidity.hashCode(),
                this.customers.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof Bank))
            return false;

        Bank bank = (Bank) object;
        if (!(this.bankName.equals(bank.getBankName())))
            return false;

        if (!(this.motto.equals(bank.getMotto())))
            return false;

        if (this.foundingYear != bank.getFoundingYear())
            return false;

        if (!(this.phoneNumber.equals(bank.getPhoneNumber())))
            return false;

        if (!(this.emailAddress.equals(bank.emailAddress)))
            return false;

        if (!(this.website.equals(bank.website)))
            return false;

        if (this.hashCode() != bank.hashCode())
            return false;

        return true;
    }

    public void createLoan(Customer customer,
                           CurrentAccount currentAccount,
                           double amount,
                           int maturity) {
        Loan loan = new Loan(customer.getCustomerType(), currentAccount, amount, maturity);
        customer.getProducts().add(loan);
        this.modifyLiquidity(currentAccount.getCurrency(), -amount);
        currentAccount.makeTransaction(amount, TransactionType.CREDIT, TransactionDetail.LOAN_GRANTING);
    }

    public void createDeposit(Customer customer,
                              CurrentAccount currentAccount,
                              double depositAmount,
                              int maturity) {
        Deposit deposit = new Deposit(currentAccount, maturity, depositAmount);
        customer.getProducts().add(deposit);
        this.modifyLiquidity(currentAccount.getCurrency(), depositAmount);
        currentAccount.makeTransaction(depositAmount, TransactionType.DEBIT, TransactionDetail.CREATE_DEPOSIT);
    }

    public boolean addMoneyToCurrentAccount(double amount,
                                            Currency currency,
                                            String iban,
                                            TransactionDetail transactionDetail,
                                            LocalDate date) {
        boolean status = false;

        for (Customer customer : this.customers)
            for (Product product : customer.getProducts())
                if (product instanceof CurrentAccount)
                    if (product.getCurrency().equals(currency))
                        if (product.getProductUniqueId().equals(iban)) {
                            ((CurrentAccount) product).makeTransaction(amount, TransactionType.CREDIT, transactionDetail, date);
                            // iban is unique
                            return true;
                        }

        return status;
    }


    public void performExchangeRateService(CurrentAccount fromCurrentAccount,
                                           double amountPaid,
                                           CurrentAccount toCurrentAccount,
                                           double amountReceived) {
        fromCurrentAccount.makeTransaction(
                amountPaid,
                TransactionType.DEBIT,
                TransactionDetail.CURRENCY_EXCHAGE_PAID);
        this.modifyLiquidity(fromCurrentAccount.getCurrency(), amountPaid);
        toCurrentAccount.makeTransaction(
                amountReceived,
                TransactionType.CREDIT,
                TransactionDetail.CURRENCY_EXCHANGE_RECEIVED
        );
        this.modifyLiquidity(toCurrentAccount.getCurrency(), -amountReceived);
    }

    public void runInConsole() {
        ConsoleMenus.run(this);
    }

    public List<Customer> getCustomers() {
        return this.customers;
    }
}
