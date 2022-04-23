package exceptions;

public class InvalidDepositMaturityException extends IllegalArgumentException {
    public InvalidDepositMaturityException(String errorMessage) {
        super(errorMessage);
    }
}
