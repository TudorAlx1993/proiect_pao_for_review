package exceptions;

public class WeakPasswordException extends Exception {
    public WeakPasswordException(String errorMessage) {
        super(errorMessage);
    }

    public WeakPasswordException() {
        super("Error: the selected password does not meet the security requirments!");
    }
}
