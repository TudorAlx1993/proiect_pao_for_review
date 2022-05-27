package io;

import configs.Codes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                "email_address varchar(30) not null, " +
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
                "maturity_in_months int not null, " +
                "associated_iban varchar(34) not null, " +
                "constraint deposit_id_constraint check (char_length(deposit_id)=20), " +
                "constraint deposit_amount_constraint check (deposit_amount>0), " +
                "constraint deposit_interest_rate_constraint check (interest_rate>0), " +
                "constraint deposit_maturity_in_months_constraint check (maturity_in_months>0), " +
                "constraint deposit_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban)" +
                ")");
        sqlCreateTableCommands.add("create table if not exists debit_cards" +
                "(" +
                "card_id varchar(16) primary key, " +
                "opening_date date not null, " +
                "years_to_expiration_date int not null, " +
                "hash_of_pin varchar(64) not null, " +
                "name_on_card varchar(30) not null, " +
                "network_processor_name varchar(15) not null, " +
                "associated_iban varchar(34) not null, " +
                "constraint card_id_constraint check (char_length(card_id)=16), " +
                "constraint years_to_expiration_date_constraint check (years_to_expiration_date>0), " +
                "constraint hash_of_pin_constraint check (char_length(hash_of_pin)=64), " +
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
                "constraint amount_constraint check (amount>0), " +
                "constraint current_account_transactions_associated_iban_fk_constraint foreign key (associated_iban) references current_accounts(iban)" +
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
}
