package utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class AmountFormatter {
    private AmountFormatter() {

    }

    public static String format(double amount) {
        final String pattern = "###,###.##";
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.applyPattern(pattern);

        return decimalFormat.format(amount);
    }
}
