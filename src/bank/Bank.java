package bank;

import address.Address;
import audit.AuditService;
import audit.UserType;
import configs.*;
import currency.Currency;
import customers.Company;
import customers.Customer;
import customers.CustomerType;
import customers.Individual;
import io.CsvFileReader;
import io.CsvFileWriter;
import products.*;
import services.ExchangeRateService;
import transaction.TransactionDetail;
import transaction.TransactionLogger;
import transaction.TransactionType;
import utils.AmountFormatter;
import utils.DateFromString;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

        AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed customers");
    }

    @Override
    public void sortCustomersByNoProductsDesc() {
        Collections.sort(this.customers);
        AuditService.addLoggingData(UserType.BANK_MANAGER, "sorted customers by number of products (desc)");
    }

    @Override
    public void showCustomersSummary() {
        System.out.println("\nCustomers details (summary):");
        System.out.println("\t* total customers (including deleted): " + Customer.getNoOfCustomers());
        System.out.println("\t* total active customers: " + this.customers.size());

        AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed the summary of customers");
    }

    @Override
    public void showSystemDate() {
        System.out.println("System date: " + SystemDate.getDate());
        AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed the system date");
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

            customer = new Individual(firstName, lastName, cnp, password, phoneNumber, emailAddress, address, false);
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

            customer = new Company(companyName, cui, LocalDate.of(year, month, day), password, phoneNumber, emailAddress, address, false);
        }

        this.customers.add(customer);

        AuditService.addLoggingData(UserType.BANK_MANAGER, "added a new customer: " + customer.getCustomerName());
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

        AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed the summary of products");
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

        AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed the summary of liquidity");
    }

    @Override
    public void setExchangeRate(Currency currency, double exchangeRate) {
        if (exchangeRate <= 0) {
            System.out.println("Bank message: operation not completed (the exchange rate must be positive).");
            return;
        }

        ExchangeRatesConfig.setReferenceExchangeRateOfCurrencyPerRON(currency, exchangeRate);
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set reference exchange rate for " + currency.getCurrencyCode() + " to " + exchangeRate + " RON");
    }

    @Override
    public void setAskSpreadPercent(double askSpreadPercent) {
        if (askSpreadPercent < ExchangeRatesConfig.getMinSpreadPercent() ||
                askSpreadPercent > ExchangeRatesConfig.getMaxSpreadPercent()) {
            System.out.println("Bank message: operation not completed (spread must be between configured min and max spread).");
            return;
        }

        ExchangeRatesConfig.setAskSpreadPercent(askSpreadPercent);
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set ask spread for exchange rate to " + askSpreadPercent + "%");
    }

    @Override
    public void setBidSpreadPercent(double bidSpreadPercent) {
        if (bidSpreadPercent < ExchangeRatesConfig.getMinSpreadPercent() ||
                bidSpreadPercent > ExchangeRatesConfig.getMaxSpreadPercent()) {
            System.out.println("Bank message: operation not completed (spread must be between the configured min and max spread).");
            return;
        }

        ExchangeRatesConfig.setBidSpreadPercent(bidSpreadPercent);
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set bid spread for exchange rate to " + bidSpreadPercent + "%");
    }

    @Override
    public void setLoanInterestRate(Currency currency, double interestRate) {
        if (interestRate <= 0.0 || interestRate >= 100.0) {
            System.out.println("Bank message: operation not completed (intereset rate should be between 0 and 100).");
            return;
        }

        InterestRateConfig.setLoanInterestRate(currency, interestRate);
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set loan interest rate for " + currency.getCurrencyCode() + " to " + interestRate + "%");
    }

    @Override
    public void showFees() {
        System.out.println("Current fees:");
        System.out.println("\t* internal payment fee: " + FeesConfig.getInternalPaymentFeePercent() + "%");
        System.out.println("\t* external payment fee: " + FeesConfig.getExternalPaymentFeePercent() + "%");
        System.out.println("\t* atm withdrawn fee: " + FeesConfig.getAtmWitdrawFeePercent() + "%");

        AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed the fees");
    }

    @Override
    public void showExchangeRates() {
        ExchangeRateService.showAvailableExchangeRates();

        AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed exchange rates");
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
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set deposit interest rate for " + currency.getCurrencyCode() + " and maturity=" + maturity + " months to " + interestRate + "%");
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
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set internal payment fee to " + internalPaymentFeePercent + "%");
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
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set external payment fee to " + externalPaymentFeePercent + "%");
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
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set atm withdraw fee to " + atmWithdrawFeePercent + "%");
    }

    @Override
    public void setSystemDate(int day, int month, int year) {
        // functia asta o utilizam pentru a verifica daca sunt deposite care au ajuns la scadenta
        // sau daca un client trebuie sa plateasca dobanda + principalul pentru un credit
        LocalDate newDate = LocalDate.of(year, month, day);
        if (newDate.compareTo(SystemDate.getDate()) < 0) {
            System.out.println("Bank message: operation not completed (the system date cannot be before the current date).");
            return;
        }

        SystemDate.setDate(newDate);
        AuditService.addLoggingData(UserType.BANK_MANAGER, "set system date to " + newDate);

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
        for (Customer customer : this.customers) {
            ArrayList<Integer> productIndexes = new ArrayList<>();
            int productIndex = -1;
            for (Product product : customer.getProducts()) {
                productIndex += 1;
                if (product instanceof Loan) {
                    Loan loan = (Loan) product;
                    Currency currency = loan.getCurrency();
                    CurrentAccount currentAccount = loan.getCurrentAccount();

                    // check for each payment date up to the current date (system date)
                    while (true) {
                        int indexToNextPaymentDate = loan.getIndexToNextPaymentDate();
                        LocalDate nextPaymentDate = loan.getPaymentDates().get(indexToNextPaymentDate);

                        if (nextPaymentDate.compareTo(SystemDate.getDate()) > 0)
                            break;

                        loan.checkForUpdatedInterestRate();

                        double principal = loan.getPrincipalPaymentAmount();
                        double interest = loan.getInterestPaymentAmount();

                        if (currentAccount.checkAmountForTransaction(principal + interest)) {
                            this.modifyLiquidity(currency, principal + interest);
                            currentAccount.makeTransaction(principal, TransactionType.DEBIT, TransactionDetail.PAYMENT_FOR_LOAN, nextPaymentDate);
                            currentAccount.makeTransaction(interest, TransactionType.DEBIT, TransactionDetail.INTEREST_FOR_LOAN, nextPaymentDate);
                            loan.decreaseLoanCurrentAmount(principal);
                            loan.updateIndexToNextPayment();
                        } else if (currentAccount.checkAmountForTransaction(interest)) {
                            this.modifyLiquidity(currency, interest);
                            currentAccount.makeTransaction(interest, TransactionType.DEBIT, TransactionDetail.INTEREST_FOR_LOAN, nextPaymentDate);
                            // acum modificam scadentarul de plata intrucat clientul nu are suficienti bani pentru plata principalului
                            loan.updatePaymentDatesBecauseOfMissingCurrentPrincipalPayment();
                        } else {
                            // in situatia in care clientul nu are bani in contul curent pentru plata dobanzii
                            // presupunem ca isi face el un transfer dintr-o sursa externa in contul curent
                            customer.transferMoneyToCurrentAccount(currentAccount, interest);
                            this.modifyLiquidity(currency, interest);
                            currentAccount.makeTransaction(interest, TransactionType.DEBIT, TransactionDetail.INTEREST_FOR_LOAN, nextPaymentDate);
                            loan.updatePaymentDatesBecauseOfMissingCurrentPrincipalPayment();
                        }

                        if (indexToNextPaymentDate == (loan.getMaturityInMonths() - 1)) {
                            productIndexes.add(Integer.valueOf(productIndex));
                            break;
                        }
                    }
                }
            }
            int count = 0;
            for (Integer indexOfProductToDelete : productIndexes)
                customer.getProducts().remove(indexOfProductToDelete.intValue() - (count++));
        }
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

    private List<String> getHeaderForCsvFileOfStaticVariables() {
        return Stream.of("class_name",
                        "count_of")
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    private List<List<String>> getStaticVariables() {
        List<List<String>> staticVariables = new ArrayList<>();

        staticVariables.add(List.of("customer", String.valueOf(Customer.getNoOfCustomers())));
        staticVariables.add(List.of("deposit", String.valueOf(Deposit.getNoOfDeposits())));
        staticVariables.add(List.of("current_account", String.valueOf(CurrentAccount.getNoCurrentAccounts())));
        staticVariables.add(List.of("debit_card", String.valueOf(DebitCard.getNoDebitCards())));
        staticVariables.add(List.of("loan", String.valueOf(Loan.getNoOfLoans())));

        return staticVariables;
    }

    public void saveCustomersAndProductsToCsvFile() {
        final String directoryPath = DataStorage.getPath();

        // section 1: save the system date
        String fileName = Paths.get(directoryPath, "system_date.csv").toString();
        final List<List<String>> systemDateFileLines = new ArrayList<>();
        systemDateFileLines.add(SystemDate.getSystemDateHeaderForCsvFile());
        systemDateFileLines.add(List.of(SystemDate.getDate().toString()));
        CsvFileWriter.getInstance().saveData(fileName, systemDateFileLines);

        // section 2: save the information related static variables (count of customers and products)
        fileName = Paths.get(directoryPath, "static_variables.csv").toString();
        final List<List<String>> staticVariableFileLines = new ArrayList<>();
        staticVariableFileLines.add(this.getHeaderForCsvFileOfStaticVariables());
        staticVariableFileLines.addAll(this.getStaticVariables());
        CsvFileWriter.getInstance().saveData(fileName, staticVariableFileLines);

        // section 3: save the information related to bank's customers
        fileName = Paths.get(directoryPath, "customers.csv").toString();
        final List<List<String>> customersFileLines = new ArrayList<>();
        customersFileLines.add(Customer.getHeaderForCustomersCsvFile());
        this.customers.forEach((customer) -> {
            customersFileLines.add(customer.getCustomerDataForCsvWriting());
        });
        CsvFileWriter.getInstance().saveData(fileName, customersFileLines);

        // section 4: save the information related to the customer's products
        final Map<ProductType, List<List<String>>> productsFileLines = new HashMap<>();
        final ArrayList<CurrentAccount> currentAccounts = new ArrayList<>();
        this.customers
                .forEach((customer) -> {
                    customer.getProducts()
                            .forEach((product -> {
                                ProductType productType = product.getProductType();
                                if (!(productsFileLines.containsKey(productType))) {
                                    productsFileLines.put(productType, new ArrayList<>());
                                    productsFileLines.get(productType).add(product.getProductHeaderForCsvFile());
                                }
                                productsFileLines.get(productType).add(product.getProductDataForCsvWriting(customer.getCustomerUniqueID()));

                                if (productType == ProductType.CURRENT_ACCOUNT)
                                    currentAccounts.add((CurrentAccount) product);
                            }));
                });
        productsFileLines.forEach((key, value) -> CsvFileWriter.getInstance().saveData(Paths.get(directoryPath, key.toString() + ".csv").toString().toLowerCase(), value));

        // section 5: save the historical transactions of the current accounts
        // toate tranzactiile (indiferent de contul curent) vor fi salvata in acelasi fisier
        // pe fiecare linie din fisierul csv va fi salvat inclusiv id-ul contului curent (IBAN) pentru a sti carui cont curent este asociata o anumita tranzactie
        fileName = Paths.get(directoryPath, "transaction_logger.csv").toString();
        final List<List<String>> transactionsFileLines = new ArrayList<>();
        transactionsFileLines.add(TransactionLogger.getTransactionHeaderForCsvFile());
        currentAccounts.forEach((currentAccount -> {
            Collections.sort(currentAccount.getTransactions());
            currentAccount.getTransactions()
                    .forEach((transaction -> {
                        transactionsFileLines.add(transaction.getTransactionDataForCsvWriting(currentAccount.getIBAN()));
                    }));
        }));
        CsvFileWriter.getInstance().saveData(fileName, transactionsFileLines);
    }

    private List<String> getFileNamesFromDirectory(String directoryPath, String pattern) {
        File directory = new File(directoryPath);
        File[] filesNames = directory.listFiles();
        if (filesNames == null) {
            System.err.println("Directory " + directoryPath + " is empty!");
            System.exit(Codes.EXIT_ON_ERROR);
        }

        List<String> filesNamesWithPattern = Arrays.stream(filesNames)
                .filter(fileName -> !fileName.isDirectory())
                .map(File::toString)
                .filter(fileName -> fileName.contains(pattern))
                .toList();
        if (filesNamesWithPattern.size() == 0) {
            System.err.println("Directory " + directoryPath + " does not contain files with the specified patter (" + pattern + ")");
            System.exit(Codes.EXIT_ON_ERROR);
        }

        return filesNamesWithPattern;
    }

    public void readCustomersAndProductsFromCsvFiles() {
        final String directoryPath = DataStorage.getPath();
        List<String> csvFilesNames = this.getFileNamesFromDirectory(directoryPath, ".csv");

        // section 1: read the system date
        final String systemDateFileName = this.getFileNameBasedOnPattern(csvFilesNames, "system_date");
        this.readSystemDateFromCsvFile(systemDateFileName);

        // section 2: first read the static variables from the csv file
        final String staticVariablesFileName = this.getFileNameBasedOnPattern(csvFilesNames, "static_variables");
        this.readStaticVariablesFromCsvFile(staticVariablesFileName);

        // section 3: read the csv file related to customers
        final String customerFileName = this.getFileNameBasedOnPattern(csvFilesNames, "customers");
        final List<List<String>> fileCustomers = this.readCsvFile(customerFileName);
        final List<Customer> customers = this.createCustomersBasedOnFileContent(fileCustomers);

        // section 4: read the csv files related to products
        // also associate the products with the related customers

        // start with the current because any other product requires the information related to a given current account
        final String currentAccountsFileName = this.getFileNameBasedOnPattern(csvFilesNames, "current_account");
        final List<List<String>> fileCurrentAccounts = this.readCsvFile(currentAccountsFileName);
        // we need to associate the current accounts to the customer -> hashmap
        // because we used ordered collection both when writing and reading the first current account for each customer will be denominated in RON
        final Map<String, List<CurrentAccount>> customersAndCurrentAccounts = this.createCurrentAccountsBasedOnFileContent(fileCurrentAccounts);
        // associate the current accounts to the right customers
        customers.forEach(customer -> {
            final String customerID = customer.getCustomerUniqueID();
            List<CurrentAccount> customerCurrentAccounts =
                    customersAndCurrentAccounts
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().equals(customerID))
                            .map(Map.Entry::getValue)
                            .flatMap(Collection::stream)
                            .toList();

            customer.getProducts().addAll(customerCurrentAccounts);
        });

        // read the transactions related to the current accounts from the csv file
        final String transactionsLoggerFileName = this.getFileNameBasedOnPattern(csvFilesNames, "transaction_logger");
        final List<List<String>> fileTransactions = this.readCsvFile(transactionsLoggerFileName);
        final Map<String, List<TransactionLogger>> currentAccountsAndTransactions = this.createTransactionsBasedOnFileContent(fileTransactions);
        // associate the transactions logger to the right current acounts
        customersAndCurrentAccounts
                .values()
                .stream()
                .flatMap(Collection::stream)
                .toList()
                .forEach(currentAccount -> {
                    final String iban = currentAccount.getIBAN();
                    List<TransactionLogger> transactions = currentAccountsAndTransactions
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().equals(iban))
                            .map(Map.Entry::getValue)
                            .flatMap(Collection::stream)
                            .toList();

                    if (transactions.size() != 0)
                        currentAccount.getTransactions().addAll(transactions);
                });

        // read the debit cards from the csv file
        final String debitCardsFileName = this.getFileNameBasedOnPattern(csvFilesNames, "debit_card");
        final List<List<String>> fileDebitCards = this.readCsvFile(debitCardsFileName);
        final Map<String, List<DebitCard>> customersAndDebitCards = this.createDebitCardsBasedOnFileContent(fileDebitCards, customersAndCurrentAccounts.values().stream().flatMap(Collection::stream).toList());
        customersAndDebitCards
                .forEach((customerID, debitCards) -> {
                    customers
                            .stream()
                            .filter(customer -> customer.getUniqueID().equals(customerID))
                            .findFirst()
                            .ifPresent(customer -> customer.getProducts().addAll(debitCards));
                });

        // read the deposits form the csv file
        final String depositsFileName = this.getFileNameBasedOnPattern(csvFilesNames, "deposit");
        final List<List<String>> fileDeposits = this.readCsvFile(depositsFileName);
        final Map<String, List<Deposit>> customersAndDeposits = this.createDepositsBasedOnFileContent(fileDeposits, customersAndCurrentAccounts.values().stream().flatMap(Collection::stream).toList());
        customersAndDeposits
                .forEach((customerID, deposits) -> {
                    customers
                            .stream()
                            .filter(customer -> customer.getUniqueID().equals(customerID))
                            .findFirst()
                            .ifPresent(customer -> customer.getProducts().addAll(deposits));
                });

        // read the loans from the csv file
        final String loansFileName = this.getFileNameBasedOnPattern(csvFilesNames, "loan");
        final List<List<String>> fileLoans = this.readCsvFile(loansFileName);


    }

    private Map<String, List<Deposit>> createDepositsBasedOnFileContent(List<List<String>> depositFileContent, List<CurrentAccount> currentAccounts) {
        Map<String, List<Deposit>> customerAndDeposits = new HashMap<>();

        depositFileContent
                .forEach(line -> {
                    // see the csv file header to understand the bellow order
                    final String depositId = line.get(0);
                    final double depositedAmount = Double.parseDouble(line.get(1));
                    final double interestRate = Double.parseDouble(line.get(2));
                    final double interestAmount = Double.parseDouble(line.get(3));
                    final LocalDate maturityDate = DateFromString.get(line.get(4));
                    final String associatedIban = line.get(5);
                    final Currency currency = new Currency(line.get(6));
                    final LocalDate openDate = DateFromString.get(line.get(7));
                    final String customerId = line.get(8);

                    CurrentAccount currentAccount = currentAccounts
                            .stream()
                            .filter(account -> account.getIBAN().equals(associatedIban))
                            .toList()
                            .get(0);

                    Deposit deposit = new Deposit(currentAccount, openDate, depositId, depositedAmount, interestRate, interestAmount, maturityDate);

                    if (!customerAndDeposits.containsKey(customerId))
                        customerAndDeposits.put(customerId, new ArrayList<>());
                    customerAndDeposits.get(customerId).add(deposit);
                });

        return customerAndDeposits;
    }

    private Map<String, List<DebitCard>> createDebitCardsBasedOnFileContent(List<List<String>> debitCardFileContent, List<CurrentAccount> currentAccounts) {
        Map<String, List<DebitCard>> customerAndDebitCards = new HashMap<>();

        debitCardFileContent
                .forEach(line -> {
                    // see the csv file header to understand the bellow order
                    final String cardID = line.get(0);
                    final LocalDate expirationDate = DateFromString.get(line.get(1));
                    final String hashOfPin = line.get(2);
                    final String nameOnCard = line.get(3);
                    final String networkProcessor = line.get(4);
                    final String associatedIban = line.get(5);
                    final Currency currency = new Currency(line.get(6));
                    final LocalDate openDate = DateFromString.get(line.get(7));
                    final String customerId = line.get(8);

                    // iban is unique
                    // we always get a list with only one element
                    // currentAccounts list already contains all banks's current accounts
                    CurrentAccount currentAccount = currentAccounts
                            .stream()
                            .filter(account -> account.getIBAN().equals(associatedIban))
                            .toList()
                            .get(0);
                    DebitCard debitCard = new DebitCard(currentAccount, cardID, openDate, expirationDate, hashOfPin, nameOnCard, networkProcessor);

                    if (!customerAndDebitCards.containsKey(customerId))
                        customerAndDebitCards.put(customerId, new ArrayList<>());
                    customerAndDebitCards.get(customerId).add(debitCard);
                });

        return customerAndDebitCards;
    }

    private Map<String, List<TransactionLogger>> createTransactionsBasedOnFileContent(List<List<String>> transactionFileContent) {
        Map<String, List<TransactionLogger>> currentAccountAndTransactions = new HashMap<>();

        transactionFileContent
                .forEach(transactionLine -> {
                    // see the csv file header to understand the bellow order
                    String transactionID = transactionLine.get(0);
                    LocalDate transactionDate = DateFromString.get(transactionLine.get(1));
                    TransactionType transactionType = transactionLine.get(2).equals("DEBIT") ? TransactionType.DEBIT : TransactionType.CREDIT;
                    double amount = Double.parseDouble(transactionLine.get(3));
                    String transactionDetail = transactionLine.get(4);
                    String ibanAssociatedTo = transactionLine.get(5);

                    TransactionLogger transactionLogger = new TransactionLogger(transactionID, transactionType, amount, transactionDetail, transactionDate);

                    if (!currentAccountAndTransactions.containsKey(ibanAssociatedTo))
                        currentAccountAndTransactions.put(ibanAssociatedTo, new ArrayList<>());
                    currentAccountAndTransactions.get(ibanAssociatedTo).add(transactionLogger);
                });

        return currentAccountAndTransactions;
    }

    private Map<String, List<CurrentAccount>> createCurrentAccountsBasedOnFileContent(List<List<String>> currentAccountsFileContent) {
        Map<String, List<CurrentAccount>> customerAndCurrentAccounts = new HashMap<>();

        currentAccountsFileContent
                .forEach(currentAccountLine -> {
                    // see the csv file header to understand the bellow order
                    String iban = currentAccountLine.get(0);
                    double amount = Double.parseDouble(currentAccountLine.get(1));
                    String currencyCode = currentAccountLine.get(2);
                    LocalDate openDate = DateFromString.get(currentAccountLine.get(3));
                    String customerID = currentAccountLine.get(4);

                    CurrentAccount currentAccount = new CurrentAccount(iban, amount, new Currency(currencyCode), openDate);

                    if (!customerAndCurrentAccounts.containsKey(customerID))
                        customerAndCurrentAccounts.put(customerID, new ArrayList<>());
                    customerAndCurrentAccounts.get(customerID).add(currentAccount);
                });

        return customerAndCurrentAccounts;
    }

    private List<Customer> createCustomersBasedOnFileContent(List<List<String>> customerFileContent) {
        List<Customer> customers = new ArrayList<>();

        customerFileContent
                .forEach(customerLine -> {
                    // see the csv file header to understand the bellow order
                    CustomerType customerType = customerLine.get(0).equals("individual".toUpperCase()) ? CustomerType.INDIVIDUAL : CustomerType.COMPANY;
                    String customerID = customerLine.get(1);
                    String customerName = customerLine.get(2);
                    LocalDate birthDate = DateFromString.get(customerLine.get(3));
                    String hashOfPassword = customerLine.get(4);
                    String phoneNumber = customerLine.get(5);
                    String emailAddress = customerLine.get(6);
                    Address address = new Address(
                            customerLine.get(7),
                            customerLine.get(8),
                            customerLine.get(9),
                            customerLine.get(10),
                            Integer.parseInt(customerLine.get(11)),
                            customerLine.get(12).equals("NA") ? "" : customerLine.get(12)
                    );

                    Customer customer = null;
                    switch (customerType) {
                        case INDIVIDUAL -> {
                            String[] lastAndFirstName = customerName.split(" ");
                            String lastName = lastAndFirstName[0];
                            String firstNames = String.join(" ", Arrays.copyOfRange(lastAndFirstName, 1, lastAndFirstName.length));
                            customer = new Individual(firstNames, lastName, customerID, hashOfPassword, phoneNumber, emailAddress, address, true);
                        }
                        case COMPANY -> customer = new Company(customerName, customerID, birthDate, hashOfPassword, phoneNumber, emailAddress, address, true);
                    }

                    customers.add(customer);
                });

        return customers;
    }

    List<List<String>> readCsvFile(String fileName) {
        return CsvFileReader
                .getInstance()
                .readLines(fileName)
                .stream()
                .map(line -> Arrays.asList(line.split(CsvFileConfig.getFileSeparator())))
                .collect(Collectors.toList());
    }

    private String getFileNameBasedOnPattern(List<String> fileNames, String pattern) {
        return fileNames
                .stream()
                .filter(fileName -> fileName.contains(pattern))
                .findFirst()
                .orElse(null);
    }

    private void readStaticVariablesFromCsvFile(String fileName) {
        CsvFileReader
                .getInstance()
                .readLines(fileName)
                .forEach(line -> {
                    List<String> lineComponents = Arrays.asList(line.split(CsvFileConfig.getFileSeparator()));
                    String className = lineComponents.get(0);
                    int countOf = Integer.parseInt(lineComponents.get(1));
                    switch (className) {
                        case "customer" -> Customer.setNoOfCustomers(countOf);
                        case "deposit" -> Deposit.setNoOfDeposits(countOf);
                        case "current_account" -> CurrentAccount.setNoCurrentAccounts(countOf);
                        case "debit_card" -> DebitCard.setNoDebitCards(countOf);
                        case "loan" -> Loan.setNoOfLoans(countOf);
                    }
                });
    }

    private void readSystemDateFromCsvFile(String fileName) {
        CsvFileReader
                .getInstance()
                .readLines(fileName)
                .stream()
                .findFirst()
                .ifPresent(line -> SystemDate.setDate(DateFromString.get(line)));
    }
}

