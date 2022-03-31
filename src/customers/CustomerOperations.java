package customers;

import bank.Bank;
import products.CurrentAccount;
import products.DebitCard;
import products.Deposit;

interface CustomerOperations {
    // done
    void addCurrentAccount(String currencyCode);

    // done
    void deleteCurrentAccount(CurrentAccount currentAccount);

    // done
    void createDebitCard(CurrentAccount currentAccount,
                         String pin,
                         String networkProcessorName);

    // done
    void updateDebitCardPIN(DebitCard debitCard, String oldPIN, String newPIN);

    // done
    void deleteDebitCard(CurrentAccount currentAccount, boolean printMessage);

    // done
    void showMyProducts();

    // done
    void showMyCurrentAccounts();

    // done
    void showMyLoans();

    // done
    void showMyDeposits();

    // done
    void showMyDebitCards();

    //done
    void updatePassword(String oldPassword, String newPassword);

    //done
    void transferMoneyToCurrentAccount(CurrentAccount currentAccount, double amount);

    //done
    void createDeposit(CurrentAccount currentAccount, double depositAmount, int maturity);

    //done
    void liquidateDepositBeforeMaturity(Deposit deposit);

    //done
    void generateCurrentAccountStatement(CurrentAccount currentAccount);

    //done
    void withdrawMoneyFromATM(DebitCard debitCard,
                              String pin,
                              double amount);

    //done
    void makePayment(CurrentAccount currentAccount,
                     double amount,
                     String destinationIBAN);

    // done
    void showCurrentInterestRates();

    // done
    void applyForLoan(CurrentAccount currentAccount,
                      double requestedAmount,
                      int maturity,
                      double incomeOrTurnover);

    // done
    void askForExchangeRates();

    // done
    void performCurrencyExchange(CurrentAccount fromCurrentAccount,
                                 CurrentAccount toCurrentAccount,
                                 double amountPaid);
}
