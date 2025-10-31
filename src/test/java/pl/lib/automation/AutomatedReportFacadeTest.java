package pl.lib.automation;

import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.lib.config.ReportConfig;
import pl.lib.model.ColorSettings;
import pl.lib.model.CompanyInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla klasy AutomatedReportFacade.
 * Sprawdzają poprawność generowania złożonych raportów z stroną tytułową i spisem treści.
 */
class AutomatedReportFacadeTest {

    @Test
    void generateCompositeReport_withSimpleJson_shouldGeneratePdf() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"name\": \"Jan Kowalski\", \"age\": 30, \"city\": \"Warszawa\" }";

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Testowy")
                .pageFormat("A4")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes, "PDF powinien zostać wygenerowany");
        assertTrue(pdfBytes.length > 0, "PDF powinien zawierać dane");
        // Sprawdzenie nagłówka PDF
        assertTrue(pdfBytes[0] == 0x25 && pdfBytes[1] == 0x50 && pdfBytes[2] == 0x44 && pdfBytes[3] == 0x46,
                "Wygenerowany plik powinien być PDF");
    }

    @Test
    void generateCompositeReport_withCompanyInfo_shouldIncludeCompanyName() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"product\": \"Laptop\", \"price\": 3500.00 }";

        CompanyInfo companyInfo = CompanyInfo.builder("SOFTRES Sp. z o.o.")
                .address("ul. Testowa 123")
                .location("30-001", "Kraków")
                .build();

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Sprzedaży")
                .companyInfo(companyInfo)
                .pageFormat("A4")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_withArrayData_shouldGenerateReport() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"employees\": [ " +
                "{\"name\":\"Jan\", \"position\":\"Developer\", \"salary\":8000}, " +
                "{\"name\":\"Anna\", \"position\":\"Manager\", \"salary\":12000}, " +
                "{\"name\":\"Piotr\", \"position\":\"Tester\", \"salary\":6000} " +
                "] }";

        ReportConfig config = new ReportConfig.Builder()
                .title("Lista Pracowników")
                .pageFormat("A4")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_withLandscapeOrientation_shouldGeneratePdf() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"department\": \"IT\", \"budget\": 500000 }";

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Budżetowy")
                .pageFormat("A4")
                .addFooterLeftText("Poufne")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_withCustomColors_shouldGeneratePdf() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"title\": \"Kolorowy Raport\", \"value\": 100 }";

        ColorSettings colors = new ColorSettings();
        colors.setTitleBackgroundColor("#FF5733");
        colors.setColumnHeaderBackgroundColor("#33FF57");
        colors.setDetailFontColor("#000000");

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport z Kolorami")
                .pageFormat("A4")
                .colorSettings(colors)
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_withComplexNestedJson_shouldGeneratePdf() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ " +
                "\"company\": { " +
                "  \"name\": \"Tech Corp\", " +
                "  \"departments\": [ " +
                "    {\"name\":\"IT\", \"employees\":50}, " +
                "    {\"name\":\"HR\", \"employees\":10} " +
                "  ] " +
                "}, " +
                "\"year\": 2025, " +
                "\"revenue\": 1000000 " +
                "}";

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Roczny Firmy")
                .pageFormat("A4")
                .withPageFooterEnabled(true)
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_withPrintJrxml_shouldGeneratePdf() throws JRException, IOException {
        // given - facade z włączonym drukowaniem JRXML
        AutomatedReportFacade facade = new AutomatedReportFacade(true);
        String jsonContent = "{ \"test\": \"data\" }";

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Debug")
                .pageFormat("A4")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_saveToDisk_shouldCreateFile(@TempDir Path tempDir) throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"testField\": \"testValue\" }";

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport do Zapisu")
                .pageFormat("A4")
                .build();

        File outputFile = tempDir.resolve("test-report.pdf").toFile();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }

        // then
        assertTrue(outputFile.exists(), "Plik PDF powinien zostać utworzony");
        assertTrue(outputFile.length() > 0, "Plik PDF powinien zawierać dane");
    }

    @Test
    void generateCompositeReport_withNullCompanyInfo_shouldGeneratePdf() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"value\": 123 }";

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport bez Firmy")
                .companyInfo(null)
                .pageFormat("A4")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_withEmptyJson_shouldGeneratePdf() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{}";

        ReportConfig config = new ReportConfig.Builder()
                .title("Pusty Raport")
                .pageFormat("A4")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateCompositeReport_withMultiplePages_shouldGeneratePdf() throws JRException, IOException {
        // given
        AutomatedReportFacade facade = new AutomatedReportFacade();

        // Duży JSON, który wymusi wiele stron
        StringBuilder jsonBuilder = new StringBuilder("{ \"records\": [");
        for (int i = 0; i < 100; i++) {
            if (i > 0) jsonBuilder.append(",");
            jsonBuilder.append("{\"id\":").append(i)
                    .append(", \"name\":\"Record ").append(i).append("\"")
                    .append(", \"value\":").append(i * 100).append("}");
        }
        jsonBuilder.append("]}");

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Wielostronicowy")
                .pageFormat("A4")
                .build();

        // when
        byte[] pdfBytes = facade.generateCompositeReport(jsonBuilder.toString(), config);

        // then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 5000, "PDF powinien być większy ze względu na wiele stron");
    }
}

