package configs;

public final class CsvFileConfig {
    private static final String fileSeparator;

    static {
        fileSeparator = ",";
    }

    private CsvFileConfig() {

    }

    public static String getFileSeparator() {
        return CsvFileConfig.fileSeparator;
    }
}
