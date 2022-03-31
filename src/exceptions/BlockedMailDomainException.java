package exceptions;

public class BlockedMailDomainException extends Exception {
    public BlockedMailDomainException(String errorMessage) {
        super(errorMessage);
    }

    public BlockedMailDomainException() {
        super("Error: the bank does not communicate with its clients on this domain mail!");
    }
}
