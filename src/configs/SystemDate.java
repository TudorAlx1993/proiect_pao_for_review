package configs;

import java.time.LocalDate;

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
}
