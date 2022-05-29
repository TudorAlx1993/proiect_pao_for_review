package bank;

import currency.Currency;

public interface BankActions {
    void showCustomers();

    void showCustomersSummary();

    void showProductsSummary();

    void sortCustomersByNoProductsDesc();

    void showLiquiditySummary();

    void setExchangeRate(Currency currency, double exchangeRate);

    void setAskSpreadPercent(double askSpreadPercent);

    void setBidSpreadPercent(double bidSpreadPercent);

    void setLoanInterestRate(Currency currency,double interestRate);

    void setDepositInterestRate(Currency currency,int maturity, double interestRate);

    void setInternalPaymentFee(double internalPaymentFeePercent);

    void setExternalPaymentFee(double externalPaymentFeePercent);

    void setAtmWithdrawFee(double atmWithdrawFeePercent);

    void setSystemDate(int day,int month,int year);

    void addCustomerFromKeyboard();

    void showFees();

    void showExchangeRates();

    void showSystemDate();

    void deleteCustomer(String customerID);
}

