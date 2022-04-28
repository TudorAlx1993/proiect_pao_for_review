import address.Address;
import audit.UserType;
import configs.*;
import currency.Currency;
import customers.Company;
import customers.Individual;
import products.CurrentAccount;
import bank.Bank;
import audit.AuditService;

import java.lang.Math;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // create the bank
        Bank bank = Bank.getBank(
                "First Bank of Romania",
                "We help you to secure a better future!",
                2022,
                "+40 777 777 777",
                "contact@firstbankofromania.ro",
                "https://firstbankofromania.ro");
        System.out.println(bank);

        // define the currencies the bank will work with
        Currency ron = new Currency("RON");
        Currency eur = new Currency("EUR");
        Currency usd = new Currency("USD");
        Currency chf = new Currency("CHF");

        // add initial liquidity to our bank
        bank.modifyLiquidity(ron, Math.pow(10, 8));
        bank.modifyLiquidity(eur, Math.pow(10, 7));
        bank.modifyLiquidity(usd, Math.pow(10, 6) * 8);
        bank.modifyLiquidity(chf, Math.pow(10, 6) * 3);

        // show bank's liquidity summary
        bank.showLiquiditySummary();

        // configure the exchange rates
        ExchangeRatesConfig.setReferenceExchangeRateOfCurrencyPerRON(eur, 5.0);
        ExchangeRatesConfig.setReferenceExchangeRateOfCurrencyPerRON(usd, 4.5);
        ExchangeRatesConfig.setReferenceExchangeRateOfCurrencyPerRON(chf, 4.7);

        // configure the bid and ask spreads applied to the reference exchange rates
        ExchangeRatesConfig.setBidSpreadPercent(5);
        ExchangeRatesConfig.setAskSpreadPercent(5);

        // block communication to a given domain mail
        MailCommunication.addBlockedMailDomain("someDarkWebMailDomain.com");

        // set the interest rates for loans
        InterestRateConfig.setLoanInterestRate(ron, 10);
        InterestRateConfig.setLoanInterestRate(eur, 5);
        InterestRateConfig.setLoanInterestRate(usd, 4);
        InterestRateConfig.setLoanInterestRate(chf, 3.9);

        // set the intereset rates for deposits
        InterestRateConfig.setDepositInterestRate(ron, new int[]{1, 3, 6, 9, 12}, new double[]{2.2, 2.5, 2.8, 3, 3.2});
        InterestRateConfig.setDepositInterestRate(eur, new int[]{1, 3, 6, 9, 12}, new double[]{1.0, 1.1, 1.2, 1.3, 1.4});
        InterestRateConfig.setDepositInterestRate(usd, new int[]{1, 3, 6, 9, 12}, new double[]{0.9, 1.0, 1.1, 1.2, 1.3});
        InterestRateConfig.setDepositInterestRate(chf, new int[]{1, 3, 6, 9, 12}, new double[]{0.5, 0.6, 0.7, 0.8, 0.9});

        // show the offered interest rates by bank
        InterestRateConfig.showInterestRates();

        // create some customers (individuals)
        Individual individual1 = new Individual(
                "FirstName1",
                "LastName1",
                "1930729000000",
                "parola1",
                "0799999999",
                "firstname1.lastname1@mailserver.com.",
                new Address("Romania", "Bucharest", "060542", "Mehadia", 12)
        );
        Individual individual2 = new Individual(
                "FirstName2",
                "LastName2",
                "1910422111111",
                "parola2",
                "0700000000",
                "firstname2.lastname2@mailserver.com",
                new Address("Romania", "Bucharest", "221122", "Ceahlau", 14)
        );
        Individual individual3 = new Individual(
                "FirstName3",
                "LastName3",
                "2880304222222",
                "parola3",
                "0711111111",
                "firstname3.lastname3@mailserver.com",
                new Address("Romania", "Bucharest", "331144", "Moxa", 8)
        );
        Individual individual4 = new Individual(
                "FirstName4",
                "LastName4",
                "2950715333333",
                "parola4",
                "0722222222",
                "firstname4.lastname4@mailserver.com",
                new Address("Romania", "Piatra-Neamt", "775577", "Bistritei", 3)
        );

        // add the invididuals customers to the bank's list of customers
        bank.addCustomer(individual1);
        bank.addCustomer(individual2);
        bank.addCustomer(individual3);
        bank.addCustomer(individual4);

        // create some customers (companies)
        Company company1 = new Company(
                "SC Software integration SRL",
                "111111",
                LocalDate.of(2010, 10, 10),
                "parola11",
                "0733333333",
                "contact@softwareintegration.ro",
                new Address("Romania", "Timisoara", "557755", "Bega", 2)
        );
        Company company2 = new Company(
                "SC Software development SRL",
                "222222",
                LocalDate.of(2005, 5, 15),
                "parola22",
                "0744444444",
                "contact@softwaredevelopment.ro",
                new Address("Romania", "Brasov", "114411", "Turnului", 10)
        );
        Company company3 = new Company(
                "SC Code Masters  SA",
                "333333",
                LocalDate.of(2019, 10, 15),
                "parola33",
                "0755555555",
                "contact@codemasters.ro",
                new Address("Romania", "Bucharest", "440011", "Calea Victoriei", 100)
        );
        Company company4 = new Company(
                "SC Coding Anything  SA",
                "444444",
                LocalDate.of(2015, 10, 22),
                "parola44",
                "0788888888",
                "contact@codinganything.ro",
                new Address("Romania", "Bucharest", "050044", "Piata Unirii", 15)
        );

        // add the companies to the bank's list of customers
        bank.addCustomer(company1);
        bank.addCustomer(company2);
        bank.addCustomer(company3);
        bank.addCustomer(company4);

        // add new current accounts
        individual1.addCurrentAccount("USD");
        company4.addCurrentAccount("EUR");

        // sort bank's customers
        //bank.sortCustomersByNoProductsDesc();

        // show summary about the bank's customers
        bank.showCustomersSummary();
        bank.showCustomers();

        // make some transactions
        // any customer has a current account in RON
        // the first current account in RON is always the first product
        CurrentAccount currentAccountRON = (CurrentAccount) individual1.getProducts().get(0);
        individual1.transferMoneyToCurrentAccount(currentAccountRON, 10000);
        individual1.createDebitCard(currentAccountRON, "1234", "VISA");
        individual1.createDeposit(currentAccountRON, 5000, 12);
        individual1.createDeposit(currentAccountRON, 2000, 3);
        individual1.applyForLoan(currentAccountRON, 3000, 48, 15000);

        // run program in console interface
        //System.out.println("");
        //bank.runInConsole();

        // save the bank customers and their products to csv files
        bank.saveCustomersToCsvFile();


        // close the files related to audit
        AuditService.closeFiles();
    }
}