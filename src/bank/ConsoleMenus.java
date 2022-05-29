package bank;

import audit.AuditService;
import audit.UserType;
import configs.InterestRateConfig;
import currency.Currency;
import customers.Customer;
import products.CurrentAccount;
import products.DebitCard;
import products.Deposit;
import products.Product;
import utils.AmountFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

final class ConsoleMenus {
    // only this function should be accesible outside this class
    // public static void run(Bank bank)
    // anything else private

    private ConsoleMenus() {

    }

    private static void showLogInMenu() {
        String text = "\nOptions:\n" +
                "\t* 0) exit menu\n" +
                "\t* 1) log in as bank's manager\n" +
                "\t* 2) log in as customer\n";
        System.out.println(text);
    }

    private static void showCustomerMenu() {
        String text = "\nUse your id (CNP or CUI) and password to login";
        System.out.println(text);
    }

    private static void showCustmerAllowedInterogations() {
        String text = "\nOptions:\n" +
                "\t* 0) exit menu\n" +
                "\t* 1) show my products\n" +
                "\t* 2) show my current accounts\n" +
                "\t* 3) show my debit cards\n" +
                "\t* 4) show my deposits\n" +
                "\t* 5) show my loans\n" +
                "\t* 6) show current interest rates\n" +
                "\t* 7) ask for exchange rates\n" +
                "\t* 8) update password\n" +
                "\t* 9) add new current account\n" +
                "\t* 10) delete current acocunt\n" +
                "\t* 11) add money to current account\n" +
                "\t* 12) create new debit card\n" +
                "\t* 13) generate current account statement\n" +
                "\t* 14) make payment transfer\n" +
                "\t* 15) apply for loan\n" +
                "\t* 16) perform currency exchange\n" +
                "\t* 17) create deposit\n" +
                "\t* 18) delete debit card\n" +
                "\t* 19) update debit card PIN\n" +
                "\t* 20) withdraw money from ATM\n" +
                "\t* 21) liquidate deposit before maturity\n";
        System.out.println(text);
    }

    private static int getUserSelection() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your selection (integer number): ");
        return scanner.nextInt();
    }

    private static String getTextFromUser(String messageForUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(messageForUser);
        return scanner.nextLine();
    }

    private static double getDoubleFromUser(String messageForUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(messageForUser);
        return scanner.nextDouble();
    }

    public static void run(Bank bank) {
        while (true) {
            System.out.println(bank.toString());
            ConsoleMenus.showLogInMenu();
            int userSelection = ConsoleMenus.getUserSelection();

            switch (userSelection) {
                case 0:
                    return;
                case 1:
                    AuditService.addLoggingData(UserType.BANK_MANAGER, "logged in");
                    ConsoleMenus.allowBankManagerToModifyTheBankState(bank);
                    break;
                case 2:
                    ConsoleMenus.showCustomerMenu();
                    Customer customer = ConsoleMenus.authentificateCustomer(bank);
                    if (customer == null)
                        System.out.println("Invalid credentials! Please try again!");
                    else {
                        AuditService.addLoggingData(UserType.CUSTOMER, customer.getCustomerName() + " logged in");
                        ConsoleMenus.allowCustomerToInterogateBank(customer);
                    }
                    break;
                default:
                    System.out.println("Wrong choice. Please try again!");
            }
        }
    }

    private static void allowBankManagerToModifyTheBankState(Bank bank) {
        while (true) {
            ConsoleMenus.showBankManagerOptions();
            int userSelection = ConsoleMenus.getUserSelection();

            switch (userSelection) {
                case 0:
                    return;
                case 1:
                    bank.showCustomers();
                    break;
                case 2:
                    bank.showCustomersSummary();
                    break;
                case 3:
                    bank.showProductsSummary();
                    break;
                case 4:
                    bank.sortCustomersByNoProductsDesc();
                    break;
                case 5:
                    bank.showLiquiditySummary();
                    break;
                case 6:
                    bank.setExchangeRate(
                            new Currency(ConsoleMenus.getTextFromUser("Enter the currency code (e.g.:EUR, USD): ")),
                            ConsoleMenus.getDoubleFromUser("Enter the new exchange rate against RON: "));
                    break;
                case 7:
                    bank.setBidSpreadPercent(ConsoleMenus.getDoubleFromUser("Enter the new bid spread percent (between 0 and 100): "));
                    break;
                case 8:
                    bank.setAskSpreadPercent(ConsoleMenus.getDoubleFromUser("Enter the new ask spread percent (between 0 and 100): "));
                    break;
                case 9:
                    bank.setLoanInterestRate(
                            new Currency(ConsoleMenus.getTextFromUser("Enter the currency code (e.g.: RON, EUR): ")),
                            ConsoleMenus.getDoubleFromUser("Enter the new interest rate (between 0 and 100): "));
                    break;
                case 10:
                    bank.setDepositInterestRate(
                            new Currency(ConsoleMenus.getTextFromUser("Enter the currency code (e.g.: RON, EUR): ")),
                            (int) ConsoleMenus.getDoubleFromUser("Enter the maturity in months: "),
                            ConsoleMenus.getDoubleFromUser("Enter the new interest rate (between 0 and 100): "));
                    break;
                case 11:
                    bank.setInternalPaymentFee(ConsoleMenus.getDoubleFromUser("Set the percent of the fee for internal transactions (between 0 and 100): "));
                    break;
                case 12:
                    bank.setExternalPaymentFee(ConsoleMenus.getDoubleFromUser("Set the percent of the fee for external transactions (between 0 and 100): "));
                    break;
                case 13:
                    bank.setAtmWithdrawFee(ConsoleMenus.getDoubleFromUser("Set the percent of the fee for atm withdrawn (between 0 and 100): "));
                    break;
                case 14:
                    bank.setSystemDate(
                            (int) ConsoleMenus.getDoubleFromUser("Enter the day: "),
                            (int) ConsoleMenus.getDoubleFromUser("Enter the month: "),
                            (int) ConsoleMenus.getDoubleFromUser("Enter the year: "));
                    break;
                case 15:
                    bank.addCustomerFromKeyboard();
                    break;
                case 16:
                    bank.showExchangeRates();
                    break;
                case 17:
                    InterestRateConfig.showInterestRates();
                    AuditService.addLoggingData(UserType.BANK_MANAGER, "viewed the interest rates");
                    break;
                case 18:
                    bank.showFees();
                    break;
                case 19:
                    bank.showSystemDate();
                    break;
                case 20:
                    bank.deleteCustomer(ConsoleMenus.getCustomerIdBasedOnBankManagerSelection(bank.getCustomers()));
                    break;
                default:
                    System.out.println("Wrong choice. Please try again!");
            }
        }
    }

    private static void showBankManagerOptions() {
        String options = "Options:\n" +
                "\t 0) exit menu\n" +
                "\t 1) show customers\n" +
                "\t 2) show customer summary\n" +
                "\t 3) show products summary\n" +
                "\t 4) sort customers by number of products (descending)\n" +
                "\t 5) show bank's liquidity\n" +
                "\t 6) set reference exchange rates against RON\n" +
                "\t 7) set the bid spread for exchange rates\n" +
                "\t 8) set the ask spread for exchange rate\n" +
                "\t 9) set interest rates for loans\n" +
                "\t 10) set interest rates for deposits\n" +
                "\t 11) set payment fee for internal transactions\n" +
                "\t 12) set payment fee for external transactions\n" +
                "\t 13) set payment fee for atm withdrawn\n" +
                "\t 14) modify system date\n" +
                "\t 15) add a new customer\n" +
                "\t 16) show current exchange rates\n" +
                "\t 17) show current interest rates\n" +
                "\t 18) show current fees\n" +
                "\t 19) show system date\n" +
                "\t 20) delete customer\n";

        System.out.println(options);
    }

    private static Customer authentificateCustomer(Bank bank) {
        String userId = ConsoleMenus.getTextFromUser("Enter your id: ");
        String userPassword = ConsoleMenus.getTextFromUser("Enter your password: ");

        for (Customer customer : bank.getCustomers())
            if (customer.getUniqueID().equals(userId))
                if (customer.verifyCustomerIdentity(userPassword))
                    return customer;

        return null;
    }

    private static void allowCustomerToInterogateBank(Customer customer) {
        while (true) {
            ConsoleMenus.showCustmerAllowedInterogations();
            int userSelection = ConsoleMenus.getUserSelection();

            DebitCard debitCard;
            Deposit deposit;

            switch (userSelection) {
                case 0:
                    return;
                case 1:
                    customer.showMyProducts();
                    break;
                case 2:
                    customer.showMyCurrentAccounts();
                    break;
                case 3:
                    customer.showMyDebitCards();
                    break;
                case 4:
                    customer.showMyDeposits();
                    break;
                case 5:
                    customer.showMyLoans();
                    break;
                case 6:
                    customer.showCurrentInterestRates();
                    break;
                case 7:
                    customer.askForExchangeRates();
                    break;
                case 8:
                    customer.updatePassword(
                            ConsoleMenus.getTextFromUser("Enter your old password: "),
                            ConsoleMenus.getTextFromUser("Enter your new password: "));
                    break;
                case 9:
                    customer.addCurrentAccount(ConsoleMenus.getTextFromUser("Enter the currency code (e.g.: RON, EUR, USD, CHF): "));
                    break;
                case 10:
                    customer.deleteCurrentAccount(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the current account to delete: "));
                    break;
                case 11:
                    customer.transferMoneyToCurrentAccount(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the destination current account: "),
                            ConsoleMenus.getDoubleFromUser("Enter the specified amount: "));
                    break;
                case 12:
                    customer.createDebitCard(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the current account to be attached to the debit card: "),
                            ConsoleMenus.getTextFromUser("Enter your PIN: "),
                            ConsoleMenus.getTextFromUser("Enter the payment processor (e.g.: VISA): "));
                    break;
                case 13:
                    customer.generateCurrentAccountStatement(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the current current account to generate statement: "));
                    break;
                case 14:
                    customer.makePayment(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the source current account: "),
                            ConsoleMenus.getDoubleFromUser("Enter the amount: "),
                            ConsoleMenus.getTextFromUser("Enter the destination IBAN: "));
                    break;
                case 15:
                    customer.applyForLoan(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the current account to receive the loan: "),
                            ConsoleMenus.getDoubleFromUser("Enter the requested amount: "),
                            (int) ConsoleMenus.getDoubleFromUser("Enter the maturity (in months): "),
                            ConsoleMenus.getDoubleFromUser("Enter your monthly income or yearly turnover: "));
                    break;
                case 16:
                    customer.performCurrencyExchange(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the source current account: "),
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the destination current account: "),
                            ConsoleMenus.getDoubleFromUser("Enter the amount to convert: "));
                    break;
                case 17:
                    customer.createDeposit(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the source current account: "),
                            ConsoleMenus.getDoubleFromUser("Enter the amount to deposit: "),
                            (int) ConsoleMenus.getDoubleFromUser("Enter the deposit maturity (in months): "));
                    break;
                case 18:
                    customer.deleteDebitCard(
                            ConsoleMenus.getCurrentAccountBasedOnUserSelection(
                                    customer,
                                    "Select the current account for which the debit card will be deleted: "),
                            true);
                    break;
                case 19:
                    debitCard = getDebitCardBasedOnUserSelection(
                            customer,
                            "Select the debit card to update the PIN: ");
                    if (debitCard == null)
                        System.out.println("There are no debit cards to update the PIN");
                    else
                        customer.updateDebitCardPIN(
                                debitCard,
                                ConsoleMenus.getTextFromUser("Enter the old PIN: "),
                                ConsoleMenus.getTextFromUser("Enter the new PIN: "));
                    break;
                case 20:
                    debitCard = getDebitCardBasedOnUserSelection(
                            customer,
                            "Select debit card for ATM: ");
                    if (debitCard == null)
                        System.out.println("There are no debit cards to go to ATM.");
                    else
                        customer.withdrawMoneyFromATM(
                                debitCard,
                                ConsoleMenus.getTextFromUser("Enter PIN: "),
                                ConsoleMenus.getDoubleFromUser("Enter the amount to withdraw: "));
                    break;
                case 21:
                    deposit = ConsoleMenus.getDepositBasedOnUserSelection(
                            customer,
                            "Select the deposit to liquidate: ");
                    if (deposit == null)
                        System.out.println("There are no deposits to liquidate.");
                    else
                        customer.liquidateDepositBeforeMaturity(deposit);
                    break;
                default:
                    System.out.println("Wrong choice. Please try again!");
            }
        }
    }

    private static String getCustomerIdBasedOnBankManagerSelection(List<Customer> customers) {
        int customerIndex = 1;
        System.out.println("Bank's customers are:");
        for (Customer customer : customers)
            System.out.println("\t " + (customerIndex++) + ") ID: " + customer.getUniqueID() + " customer name: " + customer.getCustomerName());

        int bankManagerSelection;
        while (true) {
            bankManagerSelection = ConsoleMenus.getUserSelection();
            if (bankManagerSelection >= customerIndex || bankManagerSelection <= 0) {
                System.out.println("Invalid choice! Please try again!");
                continue;
            } else
                break;
        }

        return customers.get(bankManagerSelection - 1).getCustomerUniqueID();
    }

    private static CurrentAccount getCurrentAccountBasedOnUserSelection(Customer customer, String messageToUser) {
        if (messageToUser != null)
            System.out.println(messageToUser);

        System.out.println("Your current accounts are:");

        int currentAccountNumber = 1;
        List<CurrentAccount> allCurrentAccounts = new ArrayList<CurrentAccount>();

        for (Product product : customer.getProducts())
            if (product instanceof CurrentAccount) {
                CurrentAccount currentAccount = (CurrentAccount) product;
                System.out.println("\t " +
                        currentAccountNumber +
                        ") IBAN: " +
                        currentAccount.getIBAN() +
                        " currency: " +
                        currentAccount.getCurrency().getCurrencyCode() +
                        " balance: " + AmountFormatter.format(currentAccount.getAmount()));
                currentAccountNumber += 1;
                allCurrentAccounts.add(currentAccount);
            }

        int userSelection;
        while (true) {
            userSelection = ConsoleMenus.getUserSelection();
            if (userSelection >= currentAccountNumber || userSelection <= 0) {
                System.out.println("Invalid choice! Please try again!");
                continue;
            } else
                break;
        }

        return allCurrentAccounts.get(userSelection - 1);
    }

    private static DebitCard getDebitCardBasedOnUserSelection(Customer customer, String messageToUser) {
        if (messageToUser != null)
            System.out.println(messageToUser);

        System.out.println("Your debit cards are:");

        int debitCardNumber = 1;
        List<DebitCard> allDebitCards = new ArrayList<DebitCard>();

        for (Product product : customer.getProducts())
            if (product instanceof DebitCard) {
                DebitCard debitCard = (DebitCard) product;
                System.out.println("\t " +
                        debitCardNumber +
                        ") card number: " +
                        debitCard.getCardId() +
                        " associated IBAN: " +
                        debitCard.getCurrentAcount().getIBAN() +
                        " currency: " +
                        debitCard.getCurrency().getCurrencyCode());
                debitCardNumber += 1;
                allDebitCards.add(debitCard);
            }

        if (allDebitCards.size() == 0)
            return null;

        int userSelection;
        while (true) {
            userSelection = ConsoleMenus.getUserSelection();
            if (userSelection >= debitCardNumber || userSelection <= 0) {
                System.out.println("Invalid choice! Please try again!");
                continue;
            } else
                break;
        }

        return allDebitCards.get(userSelection - 1);
    }

    private static Deposit getDepositBasedOnUserSelection(Customer customer, String messageToUser) {
        if (messageToUser != null)
            System.out.println(messageToUser);

        System.out.println("Your deposits are:");

        int depositNumber = 1;
        List<Deposit> allDeposits = new ArrayList<Deposit>();

        for (Product product : customer.getProducts())
            if (product instanceof Deposit) {
                Deposit deposit = (Deposit) product;
                System.out.println("\t " +
                        depositNumber +
                        ") deposit id: " +
                        deposit.getDepositId() +
                        " amount: " +
                        deposit.getDepositAmount() +
                        " currency: " +
                        deposit.getCurrency().getCurrencyCode() +
                        " maturity date: " + deposit.getDepositMaturity());
                depositNumber += 1;
                allDeposits.add(deposit);
            }

        if (allDeposits.size() == 0)
            return null;

        int userSelection;
        while (true) {
            userSelection = ConsoleMenus.getUserSelection();
            if (userSelection >= depositNumber || userSelection <= 0) {
                System.out.println("Invalid choice! Please try again!");
                continue;
            } else
                break;
        }

        return allDeposits.get(userSelection - 1);
    }

}
