package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateFromString {
    final static String dateFormat;

    static {
        dateFormat = "yyyy-MM-dd";
    }

    private DateFromString() {

    }

    public static LocalDate get(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateFromString.dateFormat);

        return LocalDate.parse(date, dateTimeFormatter);
    }
}
