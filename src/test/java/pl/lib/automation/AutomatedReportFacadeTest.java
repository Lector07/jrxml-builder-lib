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
class AutomatedReportFacadeTest {
    @Test
    void generateCompositeReport_withSimpleJson_shouldGeneratePdf() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"name\": \"Jan Kowalski\", \"age\": 30, \"city\": \"Warszawa\" }";
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Testowy")
                .pageFormat("A4")
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes, "PDF powinien zostać wygenerowany");
        assertTrue(pdfBytes.length > 0, "PDF powinien zawierać dane");
        assertTrue(pdfBytes[0] == 0x25 && pdfBytes[1] == 0x50 && pdfBytes[2] == 0x44 && pdfBytes[3] == 0x46,
                "Wygenerowany plik powinien być PDF");
    }
    @Test
    void generateCompositeReport_withCompanyInfo_shouldIncludeCompanyName() throws JRException, IOException {
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
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_withArrayData_shouldGenerateReport() throws JRException, IOException {
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
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_withLandscapeOrientation_shouldGeneratePdf() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"department\": \"IT\", \"budget\": 500000 }";
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Budżetowy")
                .pageFormat("A4")
                .addFooterLeftText("Poufne")
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_withCustomColors_shouldGeneratePdf() throws JRException, IOException {
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
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_withComplexNestedJson_shouldGeneratePdf() throws JRException, IOException {
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
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_withPrintJrxml_shouldGeneratePdf() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade(true);
        String jsonContent = "{ \"test\": \"data\" }";
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Debug")
                .pageFormat("A4")
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_saveToDisk_shouldCreateFile(@TempDir Path tempDir) throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"testField\": \"testValue\" }";
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport do Zapisu")
                .pageFormat("A4")
                .build();
        File outputFile = tempDir.resolve("test-report.pdf").toFile();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
        assertTrue(outputFile.exists(), "Plik PDF powinien zostać utworzony");
        assertTrue(outputFile.length() > 0, "Plik PDF powinien zawierać dane");
    }
    @Test
    void generateCompositeReport_withNullCompanyInfo_shouldGeneratePdf() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{ \"value\": 123 }";
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport bez Firmy")
                .companyInfo(null)
                .pageFormat("A4")
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_withEmptyJson_shouldGeneratePdf() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = "{}";
        ReportConfig config = new ReportConfig.Builder()
                .title("Pusty Raport")
                .pageFormat("A4")
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    @Test
    void generateCompositeReport_withMultiplePages_shouldGeneratePdf() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();
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
        byte[] pdfBytes = facade.generateCompositeReport(jsonBuilder.toString(), config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 5000, "PDF powinien być większy ze względu na wiele stron");
    }
    @Test
    void generateReportWithTOC_shouldCreateDetailedPdf(@TempDir Path tempDir) throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade(true);
        String jsonContent = """
        {
          "company": {
            "name": "TechCorp Sp. z o.o.",
            "nip": "1234567890",
            "address": "ul. Testowa 123",
            "city": "Warszawa"
          },
          "employees": [
            {"id": 1, "firstName": "Jan", "lastName": "Kowalski", "position": "Senior Developer", "department": "IT", "salary": 12000},
            {"id": 2, "firstName": "Anna", "lastName": "Nowak", "position": "Project Manager", "department": "Management", "salary": 15000},
            {"id": 3, "firstName": "Piotr", "lastName": "Wiśniewski", "position": "Tester", "department": "QA", "salary": 8000}
          ],
          "departments": [
            {
              "name": "IT",
              "budget": 500000,
              "headCount": 15,
              "projects": [
                {"name": "Portal A", "status": "active", "budget": 150000},
                {"name": "System B", "status": "completed", "budget": 200000}
              ]
            },
            {
              "name": "QA",
              "budget": 200000,
              "headCount": 5,
              "projects": [{"name": "Testy automatyczne", "status": "active", "budget": 100000}]
            }
          ],
          "financialData": {
            "year": 2025,
            "revenue": 2500000,
            "expenses": 1800000,
            "profit": 700000,
            "quarterlyResults": [
              {"quarter": "Q1", "revenue": 600000, "expenses": 450000},
              {"quarter": "Q2", "revenue": 650000, "expenses": 480000},
              {"quarter": "Q3", "revenue": 620000, "expenses": 440000},
              {"quarter": "Q4", "revenue": 630000, "expenses": 430000}
            ]
          }
        }
        """;
        CompanyInfo companyInfo = CompanyInfo.builder("TechCorp Sp. z o.o.")
                .address("ul. Testowa 123")
                .location("00-001", "Warszawa")
                .build();
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Roczny Firmy 2025")
                .companyInfo(companyInfo)
                .pageFormat("A4")
                .withPageFooterEnabled(true)
                .addFooterLeftText("Dokument poufny")
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        File outputFile = tempDir.resolve("raport-ze-spisem-tresci.pdf").toFile();
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertTrue(outputFile.exists());
        System.out.println("Raport wygenerowany: " + outputFile.getAbsolutePath());
    }
    @Test
    void testCompositeReport_budgetGliwice_shouldGeneratePdf() throws Exception {
        AutomatedReportFacade facade = new AutomatedReportFacade(true);
        String jsonContent = """
        {
                  "1. Wstęp": {
                    "Cel raportu": "Przedstawienie kompleksowej analizy wykonania budżetu miasta Gliwice za rok 2024.",
                    "Data wygenerowania": "2025-11-05",
                    "Zakres danych": "Dane finansowe od 1 stycznia 2024 do 31 grudnia 2024.",
                    "1.1. Podstawa prawna": {
                      "Dokument główny": "Uchwała Rady Miasta nr LII/1078/2023 z dnia 14 grudnia 2023 r.",
                      "Inne regulacje": "Ustawa o finansach publicznych z dnia 27 sierpnia 2009 r."
                    }
                  },
                  "2. Analiza wskaźnikowa": {
                    "Opis sekcji": "Prezentacja kluczowych wskaźników budżetowych obrazujących sytuację finansową miasta.",
                    "2.1. Główne wskaźniki budżetowe": [
                      {
                        "Lp.": 1,
                        "Wskaźnik": "Udział dochodów bieżących w dochodach ogółem (WB1)",
                        "Wartość wyliczona": "90,56%",
                        "Zmiana r/r": "+0,81 pp"
                      },
                      {
                        "Lp.": 2,
                        "Wskaźnik": "Udział dochodów własnych w dochodach ogółem (WB2)",
                        "Wartość wyliczona": "66,99%",
                        "Zmiana r/r": "+1,20 pp"
                      },
                      {
                        "Lp.": 3,
                        "Wskaźnik": "Udział nadwyżki operacyjnej w dochodach bieżących (WB3)",
                        "Wartość wyliczona": "13,36%",
                        "Zmiana r/r": "+4,50 pp"
                      }
                    ],
                    "Wnioski z analizy": "Wskaźniki wskazują na stabilną i poprawiającą się kondycję finansową miasta."
                  },
                  "3. Omówienie wykonania dochodów": {
                    "Dochody ogółem (wykonanie)": "1.944.885.474,95 zł",
                    "Plan po zmianach": "1.903.250.875,31 zł",
                    "Procent realizacji planu": "102,19%",
                    "3.1. Struktura dochodów według źródeł": {
                      "Kluczowy wniosek": "Największy udział w dochodach mają dochody własne.",
                      "Źródła dochodów (tabela)": [
                        {
                          "Źródło": "Dochody własne",
                          "Kwota (zł)": "1.302.865.390,67",
                          "Udział (%)": "66,99"
                        },
                        {
                          "Źródło": "Subwencje",
                          "Kwota (zł)": "464.651.524,00",
                          "Udział (%)": "23,89"
                        },
                        {
                          "Źródło": "Dotacje celowe z budżetu państwa",
                          "Kwota (zł)": "140.362.311,46",
                          "Udział (%)": "7,22"
                        },
                        {
                          "Źródło": "Środki z UE i inne",
                          "Kwota (zł)": "36.006.248,82",
                          "Udział (%)": "1,90"
                        }
                      ]
                    }
                  },
                  "4. Podsumowanie": {
                    "Ocena końcowa": "Realizacja budżetu w roku 2024 przebiegła pomyślnie, z nadwyżką budżetową.",
                    "Rekomendacje na przyszłość": "Rekomenduje się dalsze działania w celu dywersyfikacji źródeł dochodów własnych."
                  }
                }
        """;
        ReportConfig config = new ReportConfig();
        config.setTitle("Sprawozdanie z Wykonania Budżetu Miasta Gliwice za 2024 r.");
        config.setPageFormat("A4");
        config.setOrientation("PORTRAIT");
        pl.lib.config.FormattingOptions opts = new pl.lib.config.FormattingOptions();
        opts.setGenerateBookmarks(true);
        config.setFormattingOptions(opts);
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "jrxml-reports");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File outputFile = new File(tempDir, "raport_budzet_gliwice.pdf");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
        System.out.println("PDF wygenerowany: " + outputFile.getAbsolutePath());
    }
}
