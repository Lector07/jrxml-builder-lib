// src/test/java/pl/lib/api/ReportBuilderTest.java
package pl.lib.api;

import org.junit.jupiter.api.Test;
import pl.lib.model.DataType; // Import enuma

import static org.junit.jupiter.api.Assertions.*;

public class ReportBuilderTest {

    @Test
    void testBuildsReportWithDataTypesAndPatterns() {
        // Arrange
        ReportBuilder builder = new ReportBuilder();

        // Act
        String jrxml = builder
                .withTitle("Sprawozdanie Finansowe")
                .addColumn("productName", "Nazwa Produktu", 255, DataType.STRING)
                .addColumn("quantity", "Ilość", 100, DataType.INTEGER)
                .addColumn("price", "Cena", 100, DataType.BIG_DECIMAL, "#,##0.00 zł")
                .addColumn("purchaseDate", "Data Zakupu", 100, DataType.DATE, "dd.MM.yyyy")
                .build();

        // Assert
        assertNotNull(jrxml);

        // Sprawdź definicje pól z poprawnymi typami
        assertTrue(jrxml.contains("<field name=\"productName\" class=\"java.lang.String\"/>"));
        assertTrue(jrxml.contains("<field name=\"quantity\" class=\"java.lang.Integer\"/>"));
        assertTrue(jrxml.contains("<field name=\"price\" class=\"java.math.BigDecimal\"/>"));
        assertTrue(jrxml.contains("<field name=\"purchaseDate\" class=\"java.util.Date\"/>"));

        // Sprawdź, czy wzorce formatowania zostały dodane do textField
        assertTrue(jrxml.contains("<textField pattern=\"#,##0.00 zł\""));
        assertTrue(jrxml.contains("<textField pattern=\"dd.MM.yyyy\""));

        // Sprawdź wyrównanie do prawej dla liczb
        assertTrue(jrxml.contains("<textElement textAlignment=\"Right\"/>"));

        System.out.println(jrxml);
    }

    @Test
    void testBuildsReportWithSummaries() {
        // Arrange
        ReportBuilder builder = new ReportBuilder();

        // Act
        String jrxml = builder
                .withTitle("Podsumowanie Sprzedaży")
                .addColumn("productName", "Produkt", 255, DataType.STRING, null, false)
                .addColumn("quantity", "Ilość", 150, DataType.INTEGER, "#,##0", true)
                .addColumn("totalValue", "Wartość", 150, DataType.BIG_DECIMAL, "#,##0.00 zł", true)
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
        // Arrange
        ReportBuilder builder = new ReportBuilder();

        // Act
        String jrxml = builder
                .withTitle("Raport z Paskami Zebry")
                .addColumn("name", "Nazwa", 400, DataType.STRING)
                .withZebraStriping() // <<< WŁĄCZAMY NOWĄ FUNKCJĘ
                .build();

        // Assert
        assertNotNull(jrxml);

        // 1. Sprawdź, czy definicja stylu została wygenerowana
        assertTrue(jrxml.contains("<style name=\"ZebraStripeStyle\" mode=\"Opaque\" backcolor=\"#F0F0F0\">"));
        assertTrue(jrxml.contains("<conditionExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></conditionExpression>"));

        // 2. Sprawdź, czy styl został zastosowany do elementu w bandzie <detail>
        assertTrue(jrxml.contains("<reportElement style=\"ZebraStripeStyle\""));

        System.out.println(jrxml);
    }
}