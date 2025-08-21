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
}