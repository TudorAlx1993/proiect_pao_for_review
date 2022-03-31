package exceptions;

public class InvalidLoanMaturityException extends Exception {
    public InvalidLoanMaturityException(String errorMessage) {
        super(errorMessage);
    }
}
