package io;

import address.Address;
import bank.Bank;
import configs.Codes;
import configs.DataBaseConfig;
import currency.Currency;
import customers.Company;
import customers.Customer;
import customers.CustomerType;
import customers.Individual;
import products.*;
import transaction.TransactionLogger;
import transaction.TransactionType;
import utils.DateFromString;
import utils.EncloseStringBetweenQuotes;

import javax.xml.crypto.Data;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public final class Database {
    private static final String databaseUrl;
    private static final String userName;
    private static final String userPassword;
    private static Connection databaseConnection;

    static {
        databaseUrl = "jdbc:mysql://localhost:3306/first_bank_of_romania";
        userName = "tudor";
        userPassword = "parola123456789";

        Database.establishConnection();
        Database.createDatabaseTables();
    }

    private static void establishConnection() {
        try {
            databaseConnection = DriverManager.getConnection(Database.databaseUrl, Database.userName, Database.userPassword);
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    public static Connection getDatabaseConnection() {
        return Database.databaseConnection;
    }

    public static void closeDatabaseConnection() {
        try {
            if (Database.databaseConnection != null && !Database.databaseConnection.isClosed()) {
                Database.databaseConnection.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    private static List<String> getSqlCreateTableCommands() {
        List<String> sqlCreateTableCommands = new ArrayList<>();

        sqlCreateTableCommands.add("create table if not exists customers " +
                "(" +
                "customer_id varchar(13) primary key, " +
                "customer_type varchar(10) not null, " +
                "customer_name varchar(50) not null, " +
                "birth_date date not null, " +
                "hash_of_password varchar(64) not null," +
                "phone_number varchar(10) not null, " +
                "email_address varchar(50) not null, " +
                "address_country varchar(20) not null," +
                "address_city varchar(15) not null, " +
                "address_zip_code varchar(6) not null, " +
                "address_street_name varchar(15) not null, " +
                "address_street_number int not null, " +
                "address_additional_info varchar(20), " +
                "constraint customer_type_constraint check (customer_type = 'individual' or customer_type = 'company'), " +
                "constraint customer_id_constraint check (char_length(customer_id) = 13  or  char_length(customer_id) = 6)," +
                "constraint hash_of_password_constraint check (char_length(hash_of_password) = 64 ), " +
                "constraint phone_number_constraint check (char_length(phone_number) = 10 ), " +
                "constraint address_zip_code_constraint check (char_length(address_zip_code) = 6 )" +
                ")");
        sqlCreateTableCommands.add("create table if not exists current_accounts" +
                "(" +
                "iban varchar(34) primary key, " +
                "amount double not null, " +
                "currency varchar(3) not null, " +
                "opening_date date not null, " +
                "primary_account boolean not null, " +
                "customer_id varchar(13) not null, " +
                "constraint iban_constraint check (char_length(iban)=34), " +
                "constraint currency_constraint check (char_length(currency)=3), " +
                "constraint customer_id_fk_constraint foreign key (customer_id) references customers(customer_id) on delete cascade" +
                ")");
        sqlCreateTableCommands.add("create table if not exists deposits" +
                "(" +
                "deposit_id varchar(20) primary key, " +
                "deposit_amount double not null, " +
                "interest_rate double not null, " +
                "opening_date date not null, " +
                "maturity_date date not null, " +
                "associated_iban varchar(34) not null, " +
                "constraint deposit_id_constraint check (char_length(deposit_id)=20), " +
                "constraint deposit_amount_constraint check (deposit_amount>0), " +
                "constraint deposit_interest_rate_constraint check (interest_rate>0), " +
                "constraint deposit_maturity_date_constraint check (maturity_date>opening_date), " +
                "constraint deposit_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban) on delete cascade" +
                ")");
        sqlCreateTableCommands.add("create table if not exists debit_cards" +
                "(" +
                "card_id varchar(16) primary key, " +
                "opening_date date not null, " +
                "expiration_date date not null, " +
                "hash_of_pin varchar(64) not null, " +
                "name_on_card varchar(30) not null, " +
                "network_processor_name varchar(15) not null, " +
                "associated_iban varchar(34) not null, " +
                "constraint debit_card_card_id_constraint check (char_length(card_id)=16), " +
                "constraint debit_card_expiration_date_constraint check (expiration_date>opening_date), " +
                "constraint debit_card_hash_of_pin_constraint check (char_length(hash_of_pin)=64), " +
                "constraint debit_card_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban) on delete cascade" +
                ")");
        sqlCreateTableCommands.add("create table if not exists current_account_transactions" +
                "(" +
                "transaction_id varchar(36) primary key, " +
                "transaction_date date not null, " +
                "transaction_type varchar(6) not null, " +
                "amount double not null, " +
                "transaction_detail varchar(50) not null, " +
                "associated_iban varchar(34) not null, " +
                "constraint transaction_id_constraint check (char_length(transaction_id)=36), " +
                "constraint transaction_type_constraint check (transaction_type='credit' or transaction_type='debit'), " +
                "constraint transaction_amount_constraint check (amount>=0), " +
                "constraint transaction_current_account_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban) on delete cascade" +
                ")");
        sqlCreateTableCommands.add("create table if not exists loans" +
                "(" +
                "loan_id varchar(20) primary key, " +
                "opening_date date not null, " +
                "maturity_in_months int not null, " +
                "loan_initial_amount double not null, " +
                "loan_current_amount double not null, " +
                "loan_interest_rate double not null, " +
                "index_to_next_payment int not null, " +
                "associated_iban varchar(34) not null, " +
                "constraint loan_id_constraint check (char_length(loan_id)=20), " +
                "constraint loan_maturity_in_months_constraint check (maturity_in_months>0), " +
                "constraint loan_initial_amount_constraint check (loan_initial_amount>0 and loan_initial_amount>=loan_current_amount), " +
                "constraint loan_current_amount_constraint check (loan_current_amount>=0), " +
                "constraint loan_interest_rate_constraint check (loan_interest_rate>0), " +
                "constraint index_to_next_payment_constraint check (index_to_next_payment>=0), " +
                "constraint loan_associated_iban_constraint foreign key (associated_iban) references current_accounts(iban) on delete cascade" +
                ")");

        return sqlCreateTableCommands;
    }

    private static void createDatabaseTables() {
        Database.getSqlCreateTableCommands()
                .forEach(sqlCreateTableCommand -> {
                    try {
                        Database.databaseConnection.createStatement().execute(sqlCreateTableCommand);
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                        System.exit(Codes.EXIT_ON_ERROR);
                    }
                });
    }

    private static List<Customer> readCustomers() throws SQLException {
        final List<Customer> customers = new ArrayList<>();

        final String sqlScript = "select * from customers";
        final ResultSet databaseCustomers = Database.databaseConnection.createStatement().executeQuery(sqlScript);

        while (databaseCustomers.next()) {
            final String customerId = databaseCustomers.getString(1);
            final CustomerType customerType = databaseCustomers.getString(2).equals("individual") ? CustomerType.INDIVIDUAL : CustomerType.COMPANY;
            final String customerName = databaseCustomers.getString(3);
            final String birthDate = databaseCustomers.getString(4);
            final String hashOfPassword = databaseCustomers.getString(5);
            final String phoneNumber = databaseCustomers.getString(6);
            final String emailAddress = databaseCustomers.getString(7);
            final String addressCountry = databaseCustomers.getString(8);
            final String addressCity = databaseCustomers.getString(9);
            final String addressZipCode = databaseCustomers.getString(10);
            final String addressStreetName = databaseCustomers.getString(11);
            final int addressStreetNumber = databaseCustomers.getInt(12);
            final String addressAdditionalInfo = databaseCustomers.getString(13);

            Address address = null;
            if (addressAdditionalInfo == null)
                address = new Address(addressCountry, addressCity, addressZipCode, addressStreetName, addressStreetNumber);
            else
                address = new Address(addressCountry, addressCity, addressZipCode, addressStreetName, addressStreetNumber, addressAdditionalInfo);

            Customer customer = null;
            switch (customerType) {
                case INDIVIDUAL -> {
                    String[] lastAndFirstName = customerName.split(" ");
                    String lastName = lastAndFirstName[0];
                    String firstNames = String.join(" ", Arrays.copyOfRange(lastAndFirstName, 1, lastAndFirstName.length));
                    customer = new Individual(firstNames, lastName, customerId, hashOfPassword, phoneNumber, emailAddress, address, true);
                }
                case COMPANY -> customer = new Company(customerName, customerId, DateFromString.get(birthDate), hashOfPassword, phoneNumber, emailAddress, address, true);
            }

            customers.add(customer);
        }
        return customers;
    }

    private static Map<String, List<TransactionLogger>> readCurrentAccountTransactions() throws SQLException {
        final Map<String, List<TransactionLogger>> currentAccountsAndTransactions = new HashMap<>();

        final String sqlScript = "select * from current_account_transactions;";
        final ResultSet databaseTransactions = Database.databaseConnection.createStatement().executeQuery(sqlScript);

        while (databaseTransactions.next()) {
            final String transactionID = databaseTransactions.getString(1);
            final LocalDate transactionDate = DateFromString.get(databaseTransactions.getString(2));
            final TransactionType transactionType = databaseTransactions.getString(3).equals(TransactionType.CREDIT.toString().toLowerCase()) ? TransactionType.CREDIT : TransactionType.DEBIT;
            final double amount = databaseTransactions.getDouble(4);
            final String transactionDetail = databaseTransactions.getString(5);
            final String associatedIban = databaseTransactions.getString(6);

            final TransactionLogger transaction = new TransactionLogger(transactionID, transactionType, amount, transactionDetail, transactionDate);

            if (!currentAccountsAndTransactions.containsKey(associatedIban))
                currentAccountsAndTransactions.put(associatedIban, new ArrayList<>());
            currentAccountsAndTransactions.get(associatedIban).add(transaction);
        }

        return currentAccountsAndTransactions;
    }

    public static void readCustomersAndProducts(Bank bank) {
        List<Customer> databaseCustomers = null;

        try {
            // section 1: read from database the bank's customers
            databaseCustomers = Database.readCustomers();

            // section 2: read the products from the database
            // link each product to the right customer

            // start with the current accounts because any other product requires the information related to a given current account
            Map<String, List<CurrentAccount>> customersIdAndCurrentAccounts = Database.getCustomersIdAndProducts(ProductType.CURRENT_ACCOUNT, null);
            Database.addProductsToCustomers(customersIdAndCurrentAccounts, databaseCustomers);

            // get a list with all current accounts read from database
            List<CurrentAccount> currentAccounts = Database.getAllDatabaseCurrentAccounts(customersIdAndCurrentAccounts);

            // read the deposits
            Map<String, List<Deposit>> customersIdAndDeposits = Database.getCustomersIdAndProducts(ProductType.DEPOSIT, currentAccounts);
            Database.addProductsToCustomers(customersIdAndDeposits, databaseCustomers);

            // read the debit cards
            Map<String, List<DebitCard>> customersIdAndDebitCards = Database.getCustomersIdAndProducts(ProductType.DEBIT_CARD, currentAccounts);
            Database.addProductsToCustomers(customersIdAndDebitCards, databaseCustomers);

            // read the loans
            Map<String, List<Loan>> customersIdAndLoans = Database.getCustomersIdAndProducts(ProductType.LOAN, currentAccounts);
            Database.addProductsToCustomers(customersIdAndLoans, databaseCustomers);

            // read the transactions related to the current accounts
            // link each transaction to the right current account
            Map<String, List<TransactionLogger>> ibansAndTransactions = Database.readCurrentAccountTransactions();
            Database.addTransactionsToCurrentAccounts(currentAccounts, ibansAndTransactions);
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }

        // section 3: link the customers read from mysql database to the bank's customers data structure
        bank.setCustomers(databaseCustomers);
    }

    private static <T extends Product> Map<String, List<T>>
    getCustomersIdAndProducts(ProductType productType,
                              List<CurrentAccount> currentAccounts) throws SQLException {
        Map<String, List<T>> customersIdAndProducts = new HashMap<>();

        String sqlScript = null;
        ResultSet databaseProducts = null;
        CurrentAccount currentAccount = null;
        T product = null;

        switch (productType) {
            case DEBIT_CARD -> {
                sqlScript = "select a.*, c.customer_id " +
                        "from debit_cards a " +
                        "inner join current_accounts b on a.associated_iban=b.iban " +
                        "inner join customers c on b.customer_id=c.customer_id;";
                databaseProducts = Database.databaseConnection.createStatement().executeQuery(sqlScript);

                while (databaseProducts.next()) {
                    final String cardID = databaseProducts.getString(1);
                    final LocalDate openingDate = DateFromString.get(databaseProducts.getString(2));
                    final LocalDate expirationDate = DateFromString.get(databaseProducts.getString(3));
                    final String hashOfPin = databaseProducts.getString(4);
                    final String nameOnCard = databaseProducts.getString(5);
                    final String networkProcessorName = databaseProducts.getString(6);
                    final String associatedIban = databaseProducts.getString(7);
                    final String customerID = databaseProducts.getString(8);

                    currentAccount = Database.getCurrentAccount(currentAccounts, associatedIban);

                    final DebitCard debitCard = new DebitCard(currentAccount,
                            cardID,
                            openingDate,
                            expirationDate,
                            hashOfPin,
                            nameOnCard,
                            networkProcessorName);
                    product = (T) debitCard;

                    if (!customersIdAndProducts.containsKey(customerID))
                        customersIdAndProducts.put(customerID, new ArrayList<>());
                    customersIdAndProducts.get(customerID).add(product);
                }
            }
            case CURRENT_ACCOUNT -> {
                sqlScript = "select * from current_accounts";
                databaseProducts = Database.databaseConnection.createStatement().executeQuery(sqlScript);

                while (databaseProducts.next()) {
                    final String iban = databaseProducts.getString(1);
                    final double amount = databaseProducts.getDouble(2);
                    final Currency currency = new Currency(databaseProducts.getString(3));
                    final LocalDate openingDate = DateFromString.get(databaseProducts.getString(4));
                    final boolean primaryAccount = databaseProducts.getBoolean(5);
                    final String customerID = databaseProducts.getString(6);

                    product = (T) (new CurrentAccount(iban, amount, currency, openingDate));

                    if (!customersIdAndProducts.containsKey(customerID))
                        customersIdAndProducts.put(customerID, new ArrayList<>());

                    // the primary account should always be the first product of a customer
                    if (primaryAccount)
                        customersIdAndProducts.get(customerID).add(0, product);
                    else
                        customersIdAndProducts.get(customerID).add(product);
                }
            }
            case DEPOSIT -> {
                sqlScript = "select a.*, c.customer_id " +
                        "from deposits a " +
                        "inner join current_accounts b on a.associated_iban=b.iban " +
                        "inner join customers c on b.customer_id=c.customer_id;";
                databaseProducts = Database.databaseConnection.createStatement().executeQuery(sqlScript);

                while (databaseProducts.next()) {
                    final String depositID = databaseProducts.getString(1);
                    final double depositAmount = databaseProducts.getDouble(2);
                    final double interestRate = databaseProducts.getDouble(3);
                    final LocalDate openingDate = DateFromString.get(databaseProducts.getString(4));
                    final LocalDate maturityDate = DateFromString.get(databaseProducts.getString(5));
                    final String associatedIban = databaseProducts.getString(6);
                    final String customerID = databaseProducts.getString(7);

                    final int monthsInYear = 12;
                    final Period period = Period.between(openingDate, maturityDate);
                    final int maturityInMonths = period.getYears() * monthsInYear + period.getMonths();
                    final double interestAmount = depositAmount * interestRate / 100 * maturityInMonths / monthsInYear;

                    currentAccount = Database.getCurrentAccount(currentAccounts, associatedIban);

                    final Deposit deposit = new Deposit(currentAccount,
                            openingDate,
                            depositID,
                            depositAmount,
                            interestRate,
                            interestAmount,
                            maturityDate);
                    product = (T) deposit;

                    if (!customersIdAndProducts.containsKey(customerID))
                        customersIdAndProducts.put(customerID, new ArrayList<>());
                    customersIdAndProducts.get(customerID).add(product);
                }
            }
            case LOAN -> {
                sqlScript = "select a.*, c.customer_id " +
                        "from loans a " +
                        "inner join current_accounts b on a.associated_iban=b.iban " +
                        "inner join customers c on b.customer_id=c.customer_id;";
                databaseProducts = Database.databaseConnection.createStatement().executeQuery(sqlScript);

                while (databaseProducts.next()) {
                    final String loanID = databaseProducts.getString(1);
                    final LocalDate openDate = DateFromString.get(databaseProducts.getString(2));
                    final int maturityInMonths = databaseProducts.getInt(3);
                    final double loanInitialAmount = databaseProducts.getDouble(4);
                    final double loanCurrentAmount = databaseProducts.getDouble(5);
                    final double loanInterestRate = databaseProducts.getDouble(6);
                    final int indexToNextPayment = databaseProducts.getInt(7);
                    final String associatedIban = databaseProducts.getString(8);
                    final String customerID = databaseProducts.getString(9);

                    currentAccount = Database.getCurrentAccount(currentAccounts, associatedIban);

                    final Loan loan = new Loan(currentAccount,
                            loanID,
                            loanInterestRate,
                            maturityInMonths,
                            indexToNextPayment,
                            loanInitialAmount,
                            loanCurrentAmount,
                            openDate);
                    product = (T) loan;

                    if (!customersIdAndProducts.containsKey(customerID))
                        customersIdAndProducts.put(customerID, new ArrayList<>());
                    customersIdAndProducts.get(customerID).add(product);
                }
            }
        }

        return customersIdAndProducts;
    }

    private static <T1 extends Product, T2 extends Customer>
    void addProductsToCustomers(Map<String, List<T1>> customersIdsAndProducts, List<T2> customers) {
        customersIdsAndProducts
                .forEach((customerID, products) ->
                        customers
                                .stream()
                                .filter(customer -> customer.getUniqueID().equals(customerID))
                                .findFirst()
                                .ifPresent(customer -> customer.getProducts().addAll(products))
                );
    }

    private static CurrentAccount getCurrentAccount(List<CurrentAccount> currentAccounts, String iban) {
        // iban is unique
        // we always get a list with only one element
        // parameter currentAccounts already contains all banks's current accounts from mysql database
        // thus, this function will never return null
        return currentAccounts
                .stream()
                .filter(account -> account.getIBAN().equals(iban))
                .toList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static List<CurrentAccount> getAllDatabaseCurrentAccounts
            (Map<String, List<CurrentAccount>> customersIdAndCurrentAccounts) {
        return customersIdAndCurrentAccounts
                .values()
                .stream()
                .flatMap(Collection::stream)
                .toList();
    }

    private static void addTransactionsToCurrentAccounts(List<CurrentAccount> currentAccounts,
                                                         Map<String, List<TransactionLogger>> ibansAndTransactions) {
        currentAccounts
                .forEach(currentAccount ->
                        currentAccount.getTransactions().addAll(
                                ibansAndTransactions
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getKey().equals(currentAccount.getIBAN()))
                                        .filter(entry -> entry.getValue().size() > 0)
                                        .map(Map.Entry::getValue)
                                        .flatMap(Collection::stream)
                                        .toList()
                        ));
    }

    public static void saveNewCustomer(Customer customer) {
        final String customerID = customer.getCustomerUniqueID();
        final String customerType = customer.getCustomerType().toString().toLowerCase();
        final String customerName = customer.getCustomerName();
        final LocalDate birthDate = customer.getBirthDay();
        final String hashOfPassword = customer.getHashOfPassword();
        final String phoneNumber = customer.getPhoneNumber();
        final String emailAddress = customer.getEmailAddress();
        final String addressCountry = customer.getAddress().getCountry();
        final String addressCity = customer.getAddress().getCity();
        final String zipCode = customer.getAddress().getZipCode();
        final String addressStreetName = customer.getAddress().getStreetName();
        final int addressStreetNumber = customer.getAddress().getStreetNumber();
        final String addressAdditionalInfo = customer.getAddress().getAdditionalInfo();

        final String sqlScript = "insert into customers(customer_id,customer_type,customer_name,birth_date,hash_of_password,phone_number,email_address, address_country,address_city,address_zip_code,address_street_name,address_street_number,address_additional_info) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScript);

            preparedStatement.setString(1, customerID);
            preparedStatement.setString(2, customerType);
            preparedStatement.setString(3, customerName);
            preparedStatement.setDate(4, Date.valueOf(birthDate));
            preparedStatement.setString(5, hashOfPassword);
            preparedStatement.setString(6, phoneNumber);
            preparedStatement.setString(7, emailAddress);
            preparedStatement.setString(8, addressCountry);
            preparedStatement.setString(9, addressCity);
            preparedStatement.setString(10, zipCode);
            preparedStatement.setString(11, addressStreetName);
            preparedStatement.setInt(12, addressStreetNumber);
            preparedStatement.setString(13, addressAdditionalInfo.equals("''") ? "null" : addressAdditionalInfo);

            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    public static void saveNewProduct(Product product, Customer productOwner) {
        ProductType productType = product.getProductType();

        switch (productType) {
            case CURRENT_ACCOUNT -> {
                final CurrentAccount currentAccount = (CurrentAccount) product;

                final String iban = currentAccount.getIBAN();
                final double amount = currentAccount.getAmount();
                final String currency = currentAccount.getCurrency().getCurrencyCode();
                final LocalDate openDate = currentAccount.getOpenDate();
                // the first product is always the primary current account
                final boolean primaryAccount = ((CurrentAccount) productOwner.getProducts().get(0)).getIBAN().equals(iban);
                final String customerID = productOwner.getCustomerUniqueID();

                final String sqlScript = "insert into current_accounts(iban,amount,currency,opening_date,primary_account,customer_id) " +
                        "values(?,?,?,?,?,?)";

                try {
                    PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScript);

                    preparedStatement.setString(1, iban);
                    preparedStatement.setDouble(2, amount);
                    preparedStatement.setString(3, currency);
                    preparedStatement.setDate(4, Date.valueOf(openDate));
                    preparedStatement.setBoolean(5, primaryAccount);
                    preparedStatement.setString(6, customerID);

                    preparedStatement.executeUpdate();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    System.exit(Codes.EXIT_ON_ERROR);
                }
            }
            case DEBIT_CARD -> {
                final DebitCard debitCard = (DebitCard) product;

                final String cardId = debitCard.getCardId();
                final LocalDate openDate = debitCard.getOpenDate();
                final LocalDate expirationDate = debitCard.getExpirationDate();
                final String hashOfPin = debitCard.getHashOfPin();
                final String nameOnCard = debitCard.getNameOnCard();
                final String networkProcessorName = debitCard.getNetworkProcessorName();
                final String associatedIban = debitCard.getCurrentAcount().getIBAN();

                final String sqlScript = "insert into debit_cards(card_id,opening_date,expiration_date,hash_of_pin,name_on_card,network_processor_name,associated_iban) " +
                        "values(?,?,?,?,?,?,?)";

                try {
                    PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScript);

                    preparedStatement.setString(1, cardId);
                    preparedStatement.setDate(2, Date.valueOf(openDate));
                    preparedStatement.setDate(3, Date.valueOf(expirationDate));
                    preparedStatement.setString(4, hashOfPin);
                    preparedStatement.setString(5, nameOnCard);
                    preparedStatement.setString(6, networkProcessorName);
                    preparedStatement.setString(7, associatedIban);

                    preparedStatement.executeUpdate();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    System.exit(Codes.EXIT_ON_ERROR);
                }

            }

            case DEPOSIT -> {
                final Deposit deposit = (Deposit) product;

                final String depositID = deposit.getDepositId();
                final double depositAmount = deposit.getDepositAmount();
                final double interestRate = deposit.getInterestRate();
                final LocalDate openDate = deposit.getOpenDate();
                final LocalDate depositMaturity = deposit.getDepositMaturity();
                final String iban = deposit.getAssociatedCurrentAccount().getIBAN();

                final String sqlScript = "insert into deposits(deposit_id,deposit_amount,interest_rate,opening_date,maturity_date,associated_iban) " +
                        "values(?,?,?,?,?,?)";

                try {
                    PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScript);

                    preparedStatement.setString(1, depositID);
                    preparedStatement.setDouble(2, depositAmount);
                    preparedStatement.setDouble(3, interestRate);
                    preparedStatement.setDate(4, Date.valueOf(openDate));
                    preparedStatement.setDate(5, Date.valueOf(depositMaturity));
                    preparedStatement.setString(6, iban);

                    preparedStatement.executeUpdate();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    System.exit(Codes.EXIT_ON_ERROR);
                }
            }

            case LOAN -> {
                final Loan loan = (Loan) product;

                final String loanId = loan.getLoanId();
                final LocalDate openingDate = loan.getOpenDate();
                final int maturityInMonths = loan.getMaturityInMonths();
                final double initialAmount = loan.getLoanInitialAmount();
                final double currentAmount = loan.getLoanCurrentAmount();
                final double interestRate = loan.getInterestRate();
                final int indexToNextPayment = loan.getIndexToNextPaymentDate();
                final String associatedIban = loan.getCurrentAccount().getIBAN();

                String sqlScript = "insert into loans(loan_id,opening_date,maturity_in_months,loan_initial_amount,loan_current_amount,loan_interest_rate,index_to_next_payment,associated_iban) " +
                        "values(?,?,?,?,?,?,?,?)";

                try {
                    PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScript);

                    preparedStatement.setString(1, loanId);
                    preparedStatement.setDate(2, Date.valueOf(openingDate));
                    preparedStatement.setInt(3, maturityInMonths);
                    preparedStatement.setDouble(4, initialAmount);
                    preparedStatement.setDouble(5, currentAmount);
                    preparedStatement.setDouble(6, interestRate);
                    preparedStatement.setInt(7, indexToNextPayment);
                    preparedStatement.setString(8, associatedIban);

                    preparedStatement.executeUpdate();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    System.exit(Codes.EXIT_ON_ERROR);
                }
            }
        }
    }

    public static void saveCurrentAccountTransaction(TransactionLogger transaction, CurrentAccount currentAccount) {
        final String transactionId = transaction.getTransactionId();
        final LocalDate transactionDate = transaction.getDate();
        final String transactionType = transaction.getTransactionType();
        final double amount = transaction.getAmount();
        final String transactionDetail = transaction.getTransactionDetail();
        final String associatedIban = currentAccount.getIBAN();

        final String sqlScript = "insert into current_account_transactions(transaction_id,transaction_date,transaction_type,amount,transaction_detail,associated_iban) " +
                "values(?,?,?,?,?,?)";

        try {
            PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScript);

            preparedStatement.setString(1, transactionId);
            preparedStatement.setDate(2, Date.valueOf(transactionDate));
            preparedStatement.setString(3, transactionType);
            preparedStatement.setDouble(4, amount);
            preparedStatement.setString(5, transactionDetail);
            preparedStatement.setString(6, associatedIban);

            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    public static void deleteCustomer(String customerID) {
        // this will trigger a deletion on cascade

        final String sqlScript = "delete from customers where customer_id=?";

        try {
            PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScript);
            preparedStatement.setString(1, customerID);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    public static void deleteProduct(ProductType productType, String productID) {
        final Map<ProductType, String> sqlScripts = DataBaseConfig.getSqlDeleteScriptsPerProduct();

        if (!sqlScripts.containsKey(productType)) {
            System.err.println("Error: the database was not configured to perform delete transactions for product_type=" + productType.toString().toLowerCase() + "!");
            System.exit(Codes.EXIT_ON_ERROR);
        }

        try {
            PreparedStatement preparedStatement = Database.databaseConnection.prepareStatement(sqlScripts.get(productType));
            preparedStatement.setString(1, productID);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

}

