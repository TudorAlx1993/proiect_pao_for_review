package utils;

public final class EncloseStringBetweenQuotes {
    private EncloseStringBetweenQuotes() {

    }

    public static String get(String string) {
        return "'" + string + "'";
    }
}
