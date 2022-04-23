package exceptions;

public class InvalidCurrencyException extends Exception {
    public InvalidCurrencyException(String errorMessage) {
        super(errorMessage);
    }

    public InvalidCurrencyException() {
        super("Error: National Bank does not allow operations denominated in this currency!");
    }
}
