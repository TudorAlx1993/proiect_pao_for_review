package io;

import address.Address;
import bank.Bank;
import configs.Codes;
import currency.Currency;
import customers.Company;
import customers.Customer;
import customers.CustomerType;
import customers.Individual;
import products.CurrentAccount;
import products.DebitCard;
import products.Deposit;
import products.Product;
import utils.DateFromString;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

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
                "constraint customer_id_fk_constraint foreign key (customer_id) references customers(customer_id)" +
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
                "constraint deposit_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban)" +
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
                "constraint debit_card_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban)" +
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
                "constraint transaction_current_account_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban)" +
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
                "constraint loan_associated_iban_constraint foreign key (associated_iban) references current_accounts(iban)" +
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

    private static List<Customer> readCustomers() {
        final List<Customer> customers = new ArrayList<>();
        final String sqlScript = "select * from customers";
        try {
            ResultSet databaseCustomers = Database.databaseConnection.createStatement().executeQuery(sqlScript);

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
                int addressStreetNumber = databaseCustomers.getInt(12);
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

        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }

        return customers;
    }

    private static Map<String, List<CurrentAccount>> readCurrentAccounts() {
        final Map<String, List<CurrentAccount>> customerIdsAndCurrentAccounts = new HashMap<>();
        final String sqlScript = "select * from current_accounts";
        try {
            ResultSet databaseCurrentAccounts = Database.databaseConnection.createStatement().executeQuery(sqlScript);

            while (databaseCurrentAccounts.next()) {
                final String iban = databaseCurrentAccounts.getString(1);
                double amount = databaseCurrentAccounts.getDouble(2);
                final String currencyCode = databaseCurrentAccounts.getString(3);
                final String openingDate = databaseCurrentAccounts.getString(4);
                boolean primaryAccount = databaseCurrentAccounts.getBoolean(5);
                final String customerID = databaseCurrentAccounts.getString(6);

                CurrentAccount currentAccount = new CurrentAccount(iban, amount, new Currency(currencyCode), DateFromString.get(openingDate));

                if (!customerIdsAndCurrentAccounts.containsKey(customerID))
                    customerIdsAndCurrentAccounts.put(customerID, new ArrayList<>());

                // the primary account should always be the first product of a customer
                if (primaryAccount)
                    customerIdsAndCurrentAccounts.get(customerID).add(0, currentAccount);
                else
                    customerIdsAndCurrentAccounts.get(customerID).add(currentAccount);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }

        return customerIdsAndCurrentAccounts;
    }

    private static Map<String, List<Deposit>> readDeposits(List<CurrentAccount> currentAccounts) {
        final Map<String, List<Deposit>> customerIdsAndDeposits = new HashMap<>();
        final String sqlScript = "select a.*, c.customer_id " +
                "from deposits a " +
                "inner join current_accounts b on a.associated_iban=b.iban " +
                "inner join customers c on b.customer_id=c.customer_id;";
        try {
            ResultSet databaseDeposits = Database.databaseConnection.createStatement().executeQuery(sqlScript);
            while (databaseDeposits.next()) {
                final String depositID = databaseDeposits.getString(1);
                final double depositAmount = databaseDeposits.getDouble(2);
                final double interestRate = databaseDeposits.getDouble(3);
                final LocalDate openingDate = DateFromString.get(databaseDeposits.getString(4));
                final LocalDate maturityDate = DateFromString.get(databaseDeposits.getString(5));
                final String associatedIban = databaseDeposits.getString(6);
                final String customerID = databaseDeposits.getString(7);

                final int monthsInYear = 12;
                final Period period = Period.between(openingDate, maturityDate);
                final int maturityInMonths = period.getYears() * monthsInYear + period.getMonths();
                final double interestAmount = depositAmount * interestRate / 100 * maturityInMonths / monthsInYear;

                final CurrentAccount currentAccount = Database.getCurrentAccount(currentAccounts, associatedIban);

                Deposit deposit = new Deposit(currentAccount,
                        openingDate,
                        depositID,
                        depositAmount,
                        interestRate,
                        interestAmount,
                        maturityDate);


                if (!customerIdsAndDeposits.containsKey(customerID))
                    customerIdsAndDeposits.put(customerID, new ArrayList<>());
                customerIdsAndDeposits.get(customerID).add(deposit);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }

        return customerIdsAndDeposits;
    }

    private static Map<String, List<DebitCard>> readDebitCards(List<CurrentAccount> currentAccounts) {
        final Map<String, List<DebitCard>> customerIdsAndDebitCards = new HashMap<>();
        final String sqlScript = "select a.*, c.customer_id " +
                "from debit_cards a " +
                "inner join current_accounts b on a.associated_iban=b.iban " +
                "inner join customers c on b.customer_id=c.customer_id;";
        try {
            ResultSet databaseDebitCards = Database.databaseConnection.createStatement().executeQuery(sqlScript);
            while (databaseDebitCards.next()) {
                final String cardID = databaseDebitCards.getString(1);
                final LocalDate openingDate = DateFromString.get(databaseDebitCards.getString(2));
                final LocalDate expirationDate = DateFromString.get(databaseDebitCards.getString(3));
                final String hashOfPin = databaseDebitCards.getString(4);
                final String nameOnCard = databaseDebitCards.getString(5);
                final String networkProcessorName = databaseDebitCards.getString(6);
                final String associatedIban = databaseDebitCards.getString(7);
                final String customerID = databaseDebitCards.getString(8);

                final CurrentAccount currentAccount = Database.getCurrentAccount(currentAccounts, associatedIban);

                final DebitCard debitCard = new DebitCard(currentAccount,
                        cardID,
                        openingDate,
                        expirationDate,
                        hashOfPin,
                        nameOnCard,
                        networkProcessorName);

                if (!customerIdsAndDebitCards.containsKey(customerID))
                    customerIdsAndDebitCards.put(customerID, new ArrayList<>());
                customerIdsAndDebitCards.get(customerID).add(debitCard);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }

        return customerIdsAndDebitCards;
    }

    public static void readCustomersAndProducts(Bank bank) {
        // section 1: read from database the bank's customers
        List<Customer> databaseCustomers = Database.readCustomers();

        // section 2: read the products from the database
        // link each product to the right customer

        // start with the current accounts because any other product requires the information related to a given current account
        Map<String, List<CurrentAccount>> customerIdsAndCurrentAccounts = Database.readCurrentAccounts();
        Database.addProductsToCustomers(customerIdsAndCurrentAccounts, databaseCustomers);

        // get a list with all current accounts
        List<CurrentAccount> currentAccounts = customerIdsAndCurrentAccounts
                .values()
                .stream()
                .flatMap(Collection::stream)
                .toList();

        // read the deposits
        Map<String, List<Deposit>> customerIdsAndDeposits = Database.readDeposits(currentAccounts);
        Database.addProductsToCustomers(customerIdsAndDeposits, databaseCustomers);

        // read the debit cards
        Map<String, List<DebitCard>> customersIdsAndDebitCards = Database.readDebitCards(currentAccounts);
        Database.addProductsToCustomers(customersIdsAndDebitCards, databaseCustomers);

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
        // thus this function will never return null
        return currentAccounts
                .stream()
                .filter(account -> account.getIBAN().equals(iban))
                .toList()
                .stream()
                .findFirst()
                .orElse(null);
    }

}

