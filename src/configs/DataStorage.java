package configs;

public final class DataStorage {
    private static final String path;

    static {
        path = "./data_storage";
    }

    public static String getPath() {
        return DataStorage.path;
    }
}
