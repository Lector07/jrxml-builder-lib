package pl.lib.api;

import org.junit.jupiter.api.Test;
import pl.lib.model.DataType;

import static org.junit.jupiter.api.Assertions.*;

public class ReportBuilderAdditionalTests {

    @Test
    void testEmptyReport() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Pusty Raport")
                .build();

        assertNotNull(jrxml);
        assertTrue(jrxml.contains("<jasperReport"));
        assertTrue(jrxml.contains("<title>"));
        assertTrue(jrxml.contains("<text><![CDATA[Pusty Raport]]></text>"));
        assertFalse(jrxml.contains("<field"));
        assertFalse(jrxml.contains("<summary>"));

        System.out.println(jrxml);
    }

    @Test
    void testReportWithOnlyTitle() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Tylko Tytuł")
                .build();

        assertNotNull(jrxml);
        assertTrue(jrxml.contains("<title>"));
        assertTrue(jrxml.contains("<text><![CDATA[Tylko Tytuł]]></text>"));

        // Sprawdzenie czy generowany XML zawiera podstawowe elementy
        assertTrue(jrxml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(jrxml.contains("<jasperReport"));
        assertTrue(jrxml.contains("</jasperReport>"));

        System.out.println(jrxml);
    }

    @Test
    void testTextAlignmentBasedOnDataType() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Test Wyrównania Tekstu")
                .addColumn("name", "Nazwa", 100, DataType.STRING)
                .addColumn("amount", "Kwota", 100, DataType.BIG_DECIMAL)
                .addColumn("count", "Liczba", 100, DataType.INTEGER)
                .addColumn("date", "Data", 100, DataType.DATE)
                .build();

        // Dla kolumny z typem STRING nie powinno być wyrównania do prawej
        String stringColumnXml = jrxml.substring(jrxml.indexOf("<textField>"),
                jrxml.indexOf("</textField>") + 11);
        assertFalse(stringColumnXml.contains("textAlignment=\"Right\""));

        // Dla kolumny z typem BIG_DECIMAL i INTEGER powinno być wyrównanie do prawej
        assertTrue(jrxml.contains("<textElement textAlignment=\"Right\"/>"));

        System.out.println(jrxml);
    }

    @Test
    void testDefaultColumnWidth() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Test Domyślnej Szerokości")
                .addColumn("name", "Nazwa", DataType.STRING)
                .addColumn("description", "Opis", DataType.STRING)
                .build();

        assertNotNull(jrxml);

        // Sprawdzenie czy kolumny mają domyślną szerokość 200
        assertTrue(jrxml.contains("width=\"200\""));
        System.out.println(jrxml);
    }

    @Test
    void testCustomPageSize() {
        ReportBuilder builder = new ReportBuilder();
        int customWidth = 1000;
        int customHeight = 700;

        String jrxml = builder
                .withTitle("Niestandardowy Rozmiar Strony")
                .withPageSize(customWidth, customHeight)
                .addColumn("name", "Nazwa", 100, DataType.STRING)
                .build();

        assertNotNull(jrxml);

        // Sprawdzenie czy rozmiar strony jest poprawnie ustawiony
        assertTrue(jrxml.contains("pageWidth=\"" + customWidth + "\""));
        assertTrue(jrxml.contains("pageHeight=\"" + customHeight + "\""));
        assertTrue(jrxml.contains("columnWidth=\"" + (customWidth - 40) + "\""));
        System.out.println(jrxml);
    }

    @Test
    void testSpecialCharactersInLabels() {
        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Raport ze znakami specjalnymi: <>&\"'")
                .addColumn("special", "Znaki: <>&\"'", 100, DataType.STRING)
                .build();

        assertNotNull(jrxml);

        // Sprawdzenie czy znaki specjalne są poprawnie umieszczone w XML (w sekcji CDATA)
        assertTrue(jrxml.contains("<text><![CDATA[Raport ze znakami specjalnymi: <>&\"']]></text>"));
        assertTrue(jrxml.contains("<text><![CDATA[Znaki: <>&\"']]></text>"));

        System.out.println(jrxml);
    }

    @Test
    void testMultipleGroups() {
        // Ten test nie przejdzie w obecnej implementacji, ale pokazuje potencjalne rozszerzenie
        ReportBuilder builder = new ReportBuilder();

        // Obecna implementacja obsługuje tylko jedną grupę
        String jrxml = builder
                .withTitle("Raport z grupowaniem")
                .addColumn("department", "Dział", 100, DataType.STRING)
                .addColumn("name", "Nazwa", 200, DataType.STRING)
                .addGroup("department", "\"Dział: \" + $F{department}")
                .build();

        assertNotNull(jrxml);
        assertTrue(jrxml.contains("<group name=\"departmentGroup\">"));

        System.out.println(jrxml);
    }

    @Test
    void testFullPageWidth() {
        ReportBuilder builder = new ReportBuilder();

        // Sprawdzamy czy suma szerokości kolumn odpowiada szerokości strony
        String jrxml = builder
                .withTitle("Raport pełnej szerokości")
                .withPageSize(600, 842)
                .addColumn("col1", "Kolumna 1", 140, DataType.STRING)
                .addColumn("col2", "Kolumna 2", 140, DataType.STRING)
                .addColumn("col3", "Kolumna 3", 140, DataType.STRING)
                .addColumn("col4", "Kolumna 4", 140, DataType.STRING)
                .build();

        assertNotNull(jrxml);

        // Sprawdzenie czy kolumny są zdefiniowane z podanymi szerokościami
        assertTrue(jrxml.contains("width=\"140\""));

        // Sprawdzenie czy szerokość strony jest poprawna
        assertTrue(jrxml.contains("pageWidth=\"600\""));
        assertTrue(jrxml.contains("columnWidth=\"560\""));
    }
}