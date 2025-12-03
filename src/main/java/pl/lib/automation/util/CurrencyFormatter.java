package pl.lib.automation.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {

    private String currencySymbol;
    private char thousandsSeparator;
    private char decimalSeparator;
    private int decimalPlaces;
    private DecimalFormat amountFormat;
    private DecimalFormat percentFormat;

    public CurrencyFormatter() {
        this("zł", ' ', ',', 2);
    }

    public CurrencyFormatter(String currencySymbol, char thousandsSeparator, char decimalSeparator, int decimalPlaces) {
        this.currencySymbol = currencySymbol;
        this.thousandsSeparator = thousandsSeparator;
        this.decimalSeparator = decimalSeparator;
        this.decimalPlaces = decimalPlaces;
        initializeFormatters();
    }

    private void initializeFormatters() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(thousandsSeparator);
        symbols.setDecimalSeparator(decimalSeparator);

        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalPlaces > 0) {
            pattern.append(".");
            for (int i = 0; i < decimalPlaces; i++) {
                pattern.append("0");
            }
        }
        amountFormat = new DecimalFormat(pattern.toString(), symbols);

        DecimalFormatSymbols percentSymbols = new DecimalFormatSymbols(Locale.getDefault());
        percentSymbols.setDecimalSeparator(decimalSeparator);
        percentFormat = new DecimalFormat("0.00", percentSymbols);
    }

    public String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return formatAmount(BigDecimal.ZERO);
        }
        return amountFormat.format(amount) + " " + currencySymbol;
    }

    public String formatAmountWithoutCurrency(BigDecimal amount) {
        if (amount == null) {
            return amountFormat.format(BigDecimal.ZERO);
        }
        return amountFormat.format(amount);
    }

    public String formatPercent(BigDecimal value) {
        if (value == null) {
            return "0,00%";
        }
        BigDecimal percentValue = value.multiply(new BigDecimal("100"))
                                       .setScale(2, RoundingMode.HALF_UP);
        return percentFormat.format(percentValue) + "%";
    }

    public String formatPercentDirect(BigDecimal percentValue) {
        if (percentValue == null) {
            return "0,00%";
        }
        BigDecimal rounded = percentValue.setScale(2, RoundingMode.HALF_UP);
        return percentFormat.format(rounded) + "%";
    }

    public String calculateAndFormatExecutionPercent(BigDecimal planned, BigDecimal actual) {
        if (planned == null || planned.compareTo(BigDecimal.ZERO) == 0) {
            return "0,00%";
        }
        if (actual == null) {
            return "0,00%";
        }
        BigDecimal percent = actual.divide(planned, 4, RoundingMode.HALF_UP)
                                  .multiply(new BigDecimal("100"))
                                  .setScale(2, RoundingMode.HALF_UP);
        return percentFormat.format(percent) + "%";
    }

    public String formatDifference(BigDecimal difference) {
        if (difference == null) {
            return formatAmount(BigDecimal.ZERO);
        }
        String sign = difference.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + formatAmount(difference);
    }

    public String formatDifferenceWithoutCurrency(BigDecimal difference) {
        if (difference == null) {
            return amountFormat.format(BigDecimal.ZERO);
        }
        String sign = difference.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + amountFormat.format(difference);
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public void setThousandsSeparator(char thousandsSeparator) {
        this.thousandsSeparator = thousandsSeparator;
        initializeFormatters();
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
        initializeFormatters();
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
        initializeFormatters();
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public char getThousandsSeparator() {
        return thousandsSeparator;
    }

    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public static CurrencyFormatter forPLN() {
        return new CurrencyFormatter("zł", ' ', ',', 2);
    }

}

