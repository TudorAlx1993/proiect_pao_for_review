package exceptions;

public class NotSupportedCardNetworkProcessor extends Exception {
    public NotSupportedCardNetworkProcessor(String errorMessage) {
        super(errorMessage);
    }
}
