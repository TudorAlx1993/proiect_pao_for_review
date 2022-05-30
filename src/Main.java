import configs.*;
import currency.Currency;
import io.Database;
import bank.Bank;
import audit.AuditService;

import java.lang.Math;

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

        // read the system date and static variables from csv files
        bank.readSystemDateAndStaticVariablesFromCsvFiles();

        // read the bank customers and their products from mysql database
        bank.readCustomersAndProductsFromDatabase();

        // run program in console interface
        // example credentials: 1930729000000 and parola1
        // for more credentials please see the Main.java file from main branch on git
        System.out.println("");
        bank.runInConsole();

        // save the system date and static variables to csv files
        bank.saveSystemDateAndStaticVariablesToCsvFiles();

        // close the files related to audit
        AuditService.closeFiles();

        // close the database connection
        Database.closeDatabaseConnection();
    }
}

