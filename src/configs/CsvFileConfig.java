package configs;

public final class CsvFileConfig {
    private static final String fileSeparator;
    private static final String fileExtension;

    static {
        fileSeparator = ",";
        fileExtension=".csv";
    }

    private CsvFileConfig() {

    }

    public static String getFileSeparator() {
        return CsvFileConfig.fileSeparator;
    }

    public static String getFileExtension(){
        return CsvFileConfig.fileExtension;
    }
}
