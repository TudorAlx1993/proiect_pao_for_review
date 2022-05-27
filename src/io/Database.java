package io;

import configs.Codes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
