package io;

import bank.Bank;
import configs.DataStorage;
import configs.SystemDate;
import customers.Customer;
import products.*;
import transaction.TransactionLogger;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BankCustomersAndProductsCsvWriter {
    private Bank bank;
    private static BankCustomersAndProductsCsvWriter instance;

    private BankCustomersAndProductsCsvWriter(Bank bank) {
        this.bank=bank;
    }

    public static BankCustomersAndProductsCsvWriter getInstance(Bank bank) {
        if (BankCustomersAndProductsCsvWriter.instance == null)
            BankCustomersAndProductsCsvWriter.instance = new BankCustomersAndProductsCsvWriter(bank);

        return instance;
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

    public void save(Bank bank) {
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
        this.bank.getCustomers().forEach((customer) -> customersFileLines.add(customer.getCustomerDataForCsvWriting()));
        CsvFileWriter.getInstance().saveData(fileName, customersFileLines);

        // section 4: save the information related to the customer's products
        final Map<ProductType, List<List<String>>> productsFileLines = new HashMap<>();
        final ArrayList<CurrentAccount> currentAccounts = new ArrayList<>();
        this.bank.getCustomers()
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
}
