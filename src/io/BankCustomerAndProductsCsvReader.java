package io;

import address.Address;
import bank.Bank;
import configs.Codes;
import configs.CsvFileConfig;
import configs.DataStorage;
import configs.SystemDate;
import currency.Currency;
import customers.Company;
import customers.Customer;
import customers.CustomerType;
import customers.Individual;
import products.*;
import transaction.TransactionLogger;
import transaction.TransactionType;
import utils.DateFromString;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public final class BankCustomerAndProductsCsvReader {
    private final Bank bank;
    private static BankCustomerAndProductsCsvReader instance;

    private BankCustomerAndProductsCsvReader(Bank bank) {
        this.bank = bank;
    }

    public static BankCustomerAndProductsCsvReader getInstance(Bank bank) {
        if (BankCustomerAndProductsCsvReader.instance == null)
            BankCustomerAndProductsCsvReader.instance = new BankCustomerAndProductsCsvReader(bank);

        return BankCustomerAndProductsCsvReader.instance;
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
            System.err.println("Directory " + directoryPath + " does not contain files with the specified pattern (" + pattern + ")");
            System.exit(Codes.EXIT_ON_ERROR);
        }

        return filesNamesWithPattern;
    }

    public void read() {
        final String directoryPath = DataStorage.getPath();
        final List<String> csvFilesNames = this.getFileNamesFromDirectory(directoryPath, CsvFileConfig.getFileExtension());

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

        // start with the current accounts because any other product requires the information related to a given current account
        ProductType productType = ProductType.CURRENT_ACCOUNT;
        final String currentAccountsFileName = this.getFileNameBasedOnPattern(csvFilesNames, productType.toString().toLowerCase());
        final List<List<String>> fileCurrentAccounts = this.readCsvFile(currentAccountsFileName);
        // we need to associate each current account to the right customer -> hashmap
        // because we used ordered collection (ArrayList) both when writing and reading the first current account for each customer will be denominated in RON
        final Map<String, List<CurrentAccount>> customersIdAndCurrentAccounts = this.createProductsBasedOnFileContent(fileCurrentAccounts, productType, null);
        // associate the current accounts to the right customers
        this.addProductsToCustomers(customersIdAndCurrentAccounts, customers);

        List<CurrentAccount> currentAccounts = customersIdAndCurrentAccounts
                .values()
                .stream()
                .flatMap(Collection::stream)
                .toList();

        // read the transactions related to the current accounts from the csv file
        final String transactionsLoggerFileName = this.getFileNameBasedOnPattern(csvFilesNames, "transaction_logger");
        final List<List<String>> fileTransactions = this.readCsvFile(transactionsLoggerFileName);
        final Map<String, List<TransactionLogger>> ibansAndTransactions = this.createTransactionsBasedOnFileContent(fileTransactions);
        // associate the transactions logger to the right current acounts
        this.addTransactionsToCurrentAccounts(currentAccounts, ibansAndTransactions);

        // read the debit cards from the csv file
        productType = ProductType.DEBIT_CARD;
        final String debitCardsFileName = this.getFileNameBasedOnPattern(csvFilesNames, productType.toString().toLowerCase());
        final List<List<String>> fileDebitCards = this.readCsvFile(debitCardsFileName);
        final Map<String, List<DebitCard>> customersIdAndDebitCards = this.createProductsBasedOnFileContent(fileDebitCards, productType, currentAccounts);
        this.addProductsToCustomers(customersIdAndDebitCards, customers);

        // read the deposits form the csv file
        productType = ProductType.DEPOSIT;
        final String depositsFileName = this.getFileNameBasedOnPattern(csvFilesNames, productType.toString().toLowerCase());
        final List<List<String>> fileDeposits = this.readCsvFile(depositsFileName);
        final Map<String, List<Deposit>> customersIdAndDeposits = this.createProductsBasedOnFileContent(fileDeposits, productType, currentAccounts);
        this.addProductsToCustomers(customersIdAndDeposits, customers);

        // read the loans from the csv file
        productType = ProductType.LOAN;
        final String loansFileName = this.getFileNameBasedOnPattern(csvFilesNames, productType.toString().toLowerCase());
        final List<List<String>> fileLoans = this.readCsvFile(loansFileName);
        Map<String, List<Loan>> customersIdAndLoans = this.createProductsBasedOnFileContent(fileLoans, productType, currentAccounts);
        this.addProductsToCustomers(customersIdAndLoans, customers);

        // section 5: link the customers read form the csv file to the bank's data structure related to customers
        this.bank.setCustomers(customers);
    }

    private <T1 extends Product, T2 extends Customer>
    void addProductsToCustomers(Map<String, List<T1>> customersIdAndProducts, List<T2> customers) {
        customersIdAndProducts
                .forEach((customerID, products) -> {
                    customers
                            .stream()
                            .filter(customer -> customer.getUniqueID().equals(customerID))
                            .findFirst()
                            // the below line will always execute
                            // given the logic implemented within the method that calls this method
                            .ifPresent(customer -> customer.getProducts().addAll(products));
                });
    }

    private void addTransactionsToCurrentAccounts(List<CurrentAccount> currentAccounts,
                                                  Map<String, List<TransactionLogger>> ibansAndTransactions) {
        currentAccounts
                .forEach(currentAccount -> {
                    final String iban = currentAccount.getIBAN();
                    List<TransactionLogger> transactions = ibansAndTransactions
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().equals(iban))
                            .map(Map.Entry::getValue)
                            .flatMap(Collection::stream)
                            .toList();

                    if (transactions.size() != 0)
                        currentAccount.getTransactions().addAll(transactions);
                });
    }

    private <T extends Product> Map<String, List<T>>
    createProductsBasedOnFileContent(List<List<String>> fileContent,
                                     ProductType productType,
                                     List<CurrentAccount> currentAccounts) {
        Map<String, List<T>> customersIdsAndProducts = new HashMap<>();

        fileContent
                .forEach(line -> {
                    String customerID = null;
                    String associatedIban = null;
                    CurrentAccount currentAccount = null;
                    T product = null;

                    switch (productType) {
                        // for each switch's case please see the csv file header to understand the bellow order
                        case CURRENT_ACCOUNT -> {
                            associatedIban = line.get(0);
                            final double currentAccountAmount = Double.parseDouble(line.get(1));
                            final String currentAccountCurrencyCode = line.get(2);
                            final LocalDate currentAccountOpenDate = DateFromString.get(line.get(3));
                            customerID = line.get(4);

                            CurrentAccount currentAccountFromFile = new CurrentAccount(associatedIban,
                                    currentAccountAmount,
                                    new currency.Currency(currentAccountCurrencyCode),
                                    currentAccountOpenDate);
                            product = (T) currentAccountFromFile;
                        }
                        case DEBIT_CARD -> {
                            final String cardID = line.get(0);
                            final LocalDate cardExpirationDate = DateFromString.get(line.get(1));
                            final String cardHashOfPin = line.get(2);
                            final String nameOnCard = line.get(3);
                            final String networkProcessor = line.get(4);
                            associatedIban = line.get(5);
                            final currency.Currency cardCurrency = new currency.Currency(line.get(6));
                            final LocalDate cardOpenDate = DateFromString.get(line.get(7));
                            customerID = line.get(8);

                            currentAccount = this.getCurrentAccount(currentAccounts, associatedIban);

                            DebitCard debitCard = new DebitCard(currentAccount,
                                    cardID,
                                    cardOpenDate,
                                    cardExpirationDate,
                                    cardHashOfPin,
                                    nameOnCard,
                                    networkProcessor);
                            product = (T) debitCard;
                        }
                        case DEPOSIT -> {
                            final String depositId = line.get(0);
                            final double depositedAmount = Double.parseDouble(line.get(1));
                            final double depositInterestRate = Double.parseDouble(line.get(2));
                            final double depositInterestAmount = Double.parseDouble(line.get(3));
                            final LocalDate depositMaturityDate = DateFromString.get(line.get(4));
                            associatedIban = line.get(5);
                            final currency.Currency depositCurrency = new currency.Currency(line.get(6));
                            final LocalDate depositOpenDate = DateFromString.get(line.get(7));
                            customerID = line.get(8);

                            currentAccount = this.getCurrentAccount(currentAccounts, associatedIban);

                            Deposit deposit = new Deposit(currentAccount,
                                    depositOpenDate,
                                    depositId,
                                    depositedAmount,
                                    depositInterestRate,
                                    depositInterestAmount,
                                    depositMaturityDate);
                            product = (T) deposit;
                        }
                        case LOAN -> {
                            final String loanId = line.get(0);
                            final double loanInterestRate = Double.parseDouble(line.get(1));
                            final int loanMaturityInMonths = Integer.parseInt(line.get(2));
                            final int loanIndexToNextPayment = Integer.parseInt(line.get(3));
                            final double loanInitialAmount = Double.parseDouble(line.get(4));
                            final double loanCurrentAmount = Double.parseDouble(line.get(5));
                            associatedIban = line.get(6);
                            final currency.Currency loanCurrency = new Currency(line.get(7));
                            final LocalDate loanOpenDate = DateFromString.get(line.get(8));
                            customerID = line.get(9);

                            currentAccount = this.getCurrentAccount(currentAccounts, associatedIban);

                            Loan loan = new Loan(currentAccount,
                                    loanId,
                                    loanInterestRate,
                                    loanMaturityInMonths,
                                    loanIndexToNextPayment,
                                    loanInitialAmount,
                                    loanCurrentAmount,
                                    loanOpenDate);
                            product = (T) loan;
                        }
                    }

                    if (!customersIdsAndProducts.containsKey(customerID))
                        customersIdsAndProducts.put(customerID, new ArrayList<>());
                    customersIdsAndProducts.get(customerID).add(product);
                });

        return customersIdsAndProducts;
    }

    private CurrentAccount getCurrentAccount(List<CurrentAccount> currentAccounts, String iban) {
        // iban is unique
        // we always get a list with only one element
        // parameter currentAccounts already contains all banks's current accounts from csv file
        // thus this function will never return null
        return currentAccounts
                .stream()
                .filter(account -> account.getIBAN().equals(iban))
                .toList()
                .stream()
                .findFirst()
                .orElse(null);
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

    private List<List<String>> readCsvFile(String fileName) {
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
