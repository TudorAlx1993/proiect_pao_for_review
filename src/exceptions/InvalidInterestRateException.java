package exceptions;

public class InvalidInterestRateException extends IllegalArgumentException {
    public InvalidInterestRateException(String errorMessage) {
        super(errorMessage);
    }
}
