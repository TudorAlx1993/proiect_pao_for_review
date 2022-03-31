package exceptions;

public class InvalidExchangePairException extends Exception {
    public InvalidExchangePairException(String errorMessage) {
        super(errorMessage);
    }
}
