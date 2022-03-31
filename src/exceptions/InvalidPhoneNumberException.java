package exceptions;

public class InvalidPhoneNumberException extends Exception {
    public InvalidPhoneNumberException(String errorMessage) {
        super(errorMessage);
    }

    public InvalidPhoneNumberException() {
        super("Error: the provided phone number is invalid!");
    }
}
