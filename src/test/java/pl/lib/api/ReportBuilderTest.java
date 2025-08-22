package pl.lib.api;

import org.junit.jupiter.api.Test;
import pl.lib.model.DataType;

import static org.junit.jupiter.api.Assertions.*;

public class ReportBuilderTest {

    @Test
    void testBuildsReportWithDataTypesAndPatterns() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Sprawozdanie Finansowe")
                .addColumn("productName", "Nazwa Produktu", 255, DataType.STRING)
                .addColumn("quantity", "Ilość", 100, DataType.INTEGER)
                .addColumn("price", "Cena", 100, DataType.BIG_DECIMAL, "#,##0.00 zł")
                .addColumn("purchaseDate", "Data Zakupu", 100, DataType.DATE, "dd.MM.yyyy")
                .build();

        assertNotNull(jrxml);

        assertTrue(jrxml.contains("<field name=\"productName\" class=\"java.lang.String\"/>"));
        assertTrue(jrxml.contains("<field name=\"quantity\" class=\"java.lang.Integer\"/>"));
        assertTrue(jrxml.contains("<field name=\"price\" class=\"java.math.BigDecimal\"/>"));
        assertTrue(jrxml.contains("<field name=\"purchaseDate\" class=\"java.util.Date\"/>"));

        assertTrue(jrxml.contains("<textField pattern=\"#,##0.00 zł\""));
        assertTrue(jrxml.contains("<textField pattern=\"dd.MM.yyyy\""));

        assertTrue(jrxml.contains("<textElement textAlignment=\"Right\"/>"));

        System.out.println(jrxml);
    }

    @Test
    void testBuildsReportWithSummaries() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Podsumowanie Sprzedaży")
                .addColumn("productName", "Produkt", 255, DataType.STRING, null, false, false)
                .addColumn("quantity", "Ilość", 150, DataType.INTEGER, "#,##0.00 zł", true, false)
                .addColumn("totalValue", "Wartość", 150, DataType.BIG_DECIMAL, "#,##0.00 zł", true, true)
                .build();

        // Assert
        assertNotNull(jrxml);
        assertTrue(jrxml.contains("<variable name=\"quantity_SUM\" class=\"java.lang.Integer\" calculation=\"Sum\">"));
        assertTrue(jrxml.contains("<variable name=\"totalValue_SUM\" class=\"java.math.BigDecimal\" calculation=\"Sum\">"));
        assertTrue(jrxml.contains("<summary>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$V{quantity_SUM}]]></textFieldExpression>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$V{totalValue_SUM}]]></textFieldExpression>"));

        System.out.println(jrxml);
    }

    @Test
    void testBuildsReportWithZebraStriping() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Raport z Paskami Zebry")
                .addColumn("name", "Nazwa", 400, DataType.STRING)
                .withZebraStriping()
                .build();

        assertNotNull(jrxml);

        assertTrue(jrxml.contains("<style name=\"ZebraStripeStyle\" mode=\"Opaque\" backcolor=\"#F0F0F0\">"));
        assertTrue(jrxml.contains("<conditionExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></conditionExpression>"));

        assertTrue(jrxml.contains("<reportElement style=\"ZebraStripeStyle\""));

        System.out.println(jrxml);
    }

    @Test
    void testBuildsReportWithCustomGroupHeader() {
        // Arrange
        ReportBuilder builder = new ReportBuilder();
        String customHeaderExpression = "\"Dział: \" + $F{department}";

        // Act
        String jrxml = builder
                .withTitle("Lista Pracowników wg Działów")
                .addColumn("department", "Dział", 150, DataType.STRING)
                .addColumn("fullName", "Imię i Nazwisko", 405, DataType.STRING)
                // Używamy nowej metody addGroup z dwoma argumentami
                .addGroup("department", customHeaderExpression)
                .build();

        // Assert
        assertNotNull(jrxml);

        // Sprawdź, czy definicja grupy istnieje
        assertTrue(jrxml.contains("<group name=\"departmentGroup\">"));

        // Sprawdź, czy nagłówek grupy zawiera nasze niestandardowe wyrażenie
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[" + customHeaderExpression + "]]></textFieldExpression>"));

        System.out.println(jrxml);
    }

    @Test
    void testBuildsReportWithGroupSummaries() {
        // Arrange
        ReportBuilder builder = new ReportBuilder();

        // Act
        String jrxml = builder
                .withTitle("Raport z Podsumowaniem Grup")
                .addColumn("department", "Dział", 150, DataType.STRING)
                .addColumn("salary", "Pensja", 150, DataType.BIG_DECIMAL, "#,##0.00", false, true)
                .addGroup("department", "\"Dział: \" + $F{department}")
                .build();

        // Assert
        assertNotNull(jrxml);

        // Sprawdź, czy zmienna dla sumy w grupie została zdefiniowana
        assertTrue(jrxml.contains("<variable name=\"salary_GROUP_SUM\" class=\"java.math.BigDecimal\" resetType=\"Group\" resetGroup=\"departmentGroup\" calculation=\"Sum\">"));

        // Sprawdź, czy stopka grupy istnieje i używa tej zmiennej
        assertTrue(jrxml.contains("<groupFooter>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$V{salary_GROUP_SUM}]]></textFieldExpression>"));

        System.out.println(jrxml);
    }

    @Test
    void testBuildsReportWithStandardFooter() {
        // Arrange
        ReportBuilder builder = new ReportBuilder();
        String left = "eBudżet - ZSI \"Sprawny Urząd\"";
        String right = "BUK Softre - ww.softres.pl";

        // Act
        String jrxml = builder
                .withTitle("Raport ze Stopką")
                .addColumn("name", "Nazwa", 400, DataType.STRING)
                .withStandardFooter(left, right) // <<< WŁĄCZAMY I KONFIGURUJEMY STOPKĘ
                .build();

        // Assert
        assertNotNull(jrxml);

        // 1. Sprawdź, czy sekcja <pageFooter> została wygenerowana
        assertTrue(jrxml.contains("<pageFooter>"));

        // 2. Sprawdź, czy zawiera teksty
        assertTrue(jrxml.contains("<text><![CDATA[" + left + "\n" + right + "]]></text>"));

        // 3. Sprawdź, czy zawiera numerację stron
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[\"Strona \" + $V{PAGE_NUMBER} + \" z \"]]></textFieldExpression>"));
        assertTrue(jrxml.contains("<textField evaluationTime=\"Report\">"));

        System.out.println(jrxml);
    }
}