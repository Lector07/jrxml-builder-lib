package pl.lib.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MarginsTest {

    @Test
    public void testCustomMarginsColumnWidth() {
        // Given
        ReportBuilder builder = new ReportBuilder("TestReport");

        // When - ustawiamy niestandardowe marginesy
        builder.withMargins(50, 60, 70, 80);
        builder.withPageFormat("A4");
        builder.withHorizontalLayout(false); // PORTRAIT

        // Then - sprawdzamy czy columnWidth jest obliczany poprawnie
        int columnWidth = builder.preparePageAndGetColumnWidth();

        // A4 PORTRAIT: width = 595
        // columnWidth = 595 - leftMargin(80) - rightMargin(60) = 455
        assertEquals(455, columnWidth, "Column width should be 455 (595 - 80 - 60)");

        System.out.println("✅ Custom margins test PASSED: columnWidth = " + columnWidth);
    }

    @Test
    public void testDefaultMarginsColumnWidth() {
        // Given
        ReportBuilder builder = new ReportBuilder("TestReport");

        // When - nie ustawiamy marginesów (powinny być domyślne 20)
        builder.withPageFormat("A4");
        builder.withHorizontalLayout(false);

        // Then - sprawdzamy domyślne marginesy
        int columnWidth = builder.preparePageAndGetColumnWidth();

        // A4 PORTRAIT: width = 595
        // columnWidth = 595 - 20 - 20 = 555
        assertEquals(555, columnWidth, "Column width should be 555 (595 - 20 - 20)");

        System.out.println("✅ Default margins test PASSED: columnWidth = " + columnWidth);
    }

    @Test
    public void testLandscapeWithCustomMargins() {
        // Given
        ReportBuilder builder = new ReportBuilder("TestReport");

        // When - landscape A4 z niestandardowymi marginesami
        builder.withMargins(30, 40, 30, 40);
        builder.withPageFormat("A4");
        builder.withHorizontalLayout(true); // LANDSCAPE

        // Then
        int columnWidth = builder.preparePageAndGetColumnWidth();

        // A4 LANDSCAPE: width = 842 (zamienione z height)
        // columnWidth = 842 - leftMargin(40) - rightMargin(40) = 762
        assertEquals(762, columnWidth, "Column width should be 762 (842 - 40 - 40)");

        System.out.println("✅ Landscape margins test PASSED: columnWidth = " + columnWidth);
    }
}

