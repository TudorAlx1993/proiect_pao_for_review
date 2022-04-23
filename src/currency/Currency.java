package currency;

import configs.Codes;
import exceptions.InvalidCurrencyException;
import regulations.NationalBankRegulations;

import java.util.Objects;

// mi-am dat seama ca exista clasa java.util.Currency abia dupa ce am scris codul de mai jos :D
// imutable class
public final class Currency {
    private final String currencyCode;
    private final String currencyName;

    public Currency(String currencyCode) {
        try {
            this.checkRequirments(currencyCode);
        } catch (InvalidCurrencyException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.currencyCode = currencyCode.toUpperCase();
        this.currencyName = NationalBankRegulations.getCurrencyOfficialName(this.currencyCode);

    }

    public Currency(Currency currency) {
        this.currencyCode = currency.currencyCode;
        this.currencyName = currency.currencyName;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public String getCurrencyName() {
        return this.currencyName;
    }

    // no setters
    // I dont't know to be able to modify the state of an object once it is created
    // also, this class is assumed to be immutable

    // no need to end-user to have access to this method
    private void checkRequirments(String currencyCode) throws InvalidCurrencyException {
        for (String allowedCurrency : NationalBankRegulations.getAllowedCurrencyCodes())
            if (currencyCode.equals(allowedCurrency))
                return;

        throw new InvalidCurrencyException("Error: the National Bank does not allow operations denominated in " + currencyCode + "!");
    }

    @Override
    public String toString() {
        return this.currencyName + " (" + this.currencyCode + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.currencyCode, this.currencyName);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof Currency))
            return false;

        Currency currency = (Currency) object;
        if (!(this.currencyName.equals(currency.getCurrencyName())))
            return false;

        if (!(this.currencyCode.equals(currency.getCurrencyCode())))
            return false;

        return true;
    }
}

