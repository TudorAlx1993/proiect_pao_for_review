package exceptions;

public class CurrencyMismatchException extends Exception {
    public CurrencyMismatchException(String errorMessage) {
        super(errorMessage);
    }
}

