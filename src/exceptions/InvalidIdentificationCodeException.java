package exceptions;

public class InvalidIdentificationCodeException extends Exception {
    public InvalidIdentificationCodeException(String errorMessage) {
        super(errorMessage);
    }

    public InvalidIdentificationCodeException() {
        super("Error: the supplied identification code is not valid!");
    }
}
