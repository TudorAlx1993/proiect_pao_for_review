package configs;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public final class SystemDate {
    private static LocalDate date;

    static {
        date = LocalDate.now();
    }

    private SystemDate() {

    }

    public static LocalDate getDate() {
        return SystemDate.date;
    }

    public static void setDate(LocalDate newDate) {
        if (SystemDate.date.compareTo(newDate) <= 0)
            SystemDate.date = newDate;
    }

    public static List<String> getSystemDateHeaderForCsvFile() {
        return List.of("system_date".toUpperCase());
    }
}
