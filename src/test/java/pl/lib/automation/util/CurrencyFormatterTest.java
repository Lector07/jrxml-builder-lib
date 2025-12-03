package pl.lib.automation.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyFormatterTest {

    @Test
    void shouldFormatAmountWithThousandsSeparator() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal amount = new BigDecimal("1234567.89");
        String formatted = formatter.formatAmount(amount);
        assertEquals("1 234 567,89 zł", formatted);
    }

    @Test
    void shouldFormatSmallAmount() {
        CurrencyFormatter formatter = CurrencyFormatter.forPLN();
        BigDecimal amount = new BigDecimal("123.45");
        String formatted = formatter.formatAmount(amount);
        assertEquals("123,45 zł", formatted);
    }

    @Test
    void shouldFormatAmountWithoutCurrency() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal amount = new BigDecimal("9876543.21");
        String formatted = formatter.formatAmountWithoutCurrency(amount);
        assertEquals("9 876 543,21", formatted);
    }

    @Test
    void shouldFormatNullAsZero() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        String formatted = formatter.formatAmount(null);
        assertEquals("0,00 zł", formatted);
    }

    @Test
    void shouldFormatPercent() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal value = new BigDecimal("0.8532");
        String formatted = formatter.formatPercent(value);
        assertEquals("85,32%", formatted);
    }

    @Test
    void shouldFormatPercentDirect() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal value = new BigDecimal("94.50");
        String formatted = formatter.formatPercentDirect(value);
        assertEquals("94,50%", formatted);
    }

    @Test
    void shouldCalculateAndFormatExecutionPercent() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal planned = new BigDecimal("1000");
        BigDecimal actual = new BigDecimal("850");
        String formatted = formatter.calculateAndFormatExecutionPercent(planned, actual);
        assertEquals("85,00%", formatted);
    }

    @Test
    void shouldFormatPositiveDifference() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal difference = new BigDecimal("1234.56");
        String formatted = formatter.formatDifference(difference);
        assertEquals("+1 234,56 zł", formatted);
    }

    @Test
    void shouldFormatNegativeDifference() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal difference = new BigDecimal("-1234.56");
        String formatted = formatter.formatDifference(difference);
        assertEquals("-1 234,56 zł", formatted);
    }

    @Test
    void shouldFormatDifferenceWithoutCurrency() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal difference = new BigDecimal("500.00");
        String formatted = formatter.formatDifferenceWithoutCurrency(difference);
        assertEquals("+500,00", formatted);
    }

    @Test
    void shouldHandleZeroPlannedInExecutionPercent() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        BigDecimal planned = BigDecimal.ZERO;
        BigDecimal actual = new BigDecimal("100");
        String formatted = formatter.calculateAndFormatExecutionPercent(planned, actual);
        assertEquals("0,00%", formatted);
    }

    @Test
    void shouldHandleNullInExecutionPercent() {
        CurrencyFormatter formatter = new CurrencyFormatter();
        String formatted = formatter.calculateAndFormatExecutionPercent(null, new BigDecimal("100"));
        assertEquals("0,00%", formatted);
    }

}

