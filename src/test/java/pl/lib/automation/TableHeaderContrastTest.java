package pl.lib.automation;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.junit.jupiter.api.Test;
import pl.lib.automation.JsonReportGenerator;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test wizualny - otwórz wygenerowany PDF i SPRAWDŹ nagłówki tabel!
 */
public class TableHeaderContrastTest {

    @Test
    public void testTableHeadersAreReadable() throws Exception {
        String testJson = """
                {
                  "company_info": {
                    "name": "Test Company",
                    "revenue": 1000000
                  },
                  "employees": [
                    {"name": "Jan Kowalski", "position": "Developer", "salary": 8000},
                    {"name": "Anna Nowak", "position": "Manager", "salary": 12000},
                    {"name": "Piotr Wiśniewski", "position": "Designer", "salary": 7000}
                  ],
                  "projects": [
                    {"project_name": "Project Alpha", "budget": 50000, "status": "Active"},
                    {"project_name": "Project Beta", "budget": 75000, "status": "Completed"}
                  ]
                }
                """;

        JsonReportGenerator generator = new JsonReportGenerator();
        JasperPrint jasperPrint = generator.generateReport(testJson, "Test Czytelności Nagłówków Tabel", "Warszawa");

        File outputDir = new File("target/test-output");
        outputDir.mkdirs();
        File pdfFile = new File(outputDir, "TEST_NAGLOWKI_TABEL_KONTRAST.pdf");

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(new FileOutputStream(pdfFile)));
        exporter.exportReport();

        assertTrue(pdfFile.exists());
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 TEST WIZUALNY - SPRAWDŹ NAGŁÓWKI TABEL!");
        System.out.println("=".repeat(80));
        System.out.println("📁 PDF: " + pdfFile.getAbsolutePath());
        System.out.println("\n✅ NAGŁÓWKI POWINNY BYĆ:");
        System.out.println("   - CIEMNY GRANATOWY TEKST (#1C3A57)");
        System.out.println("   - NA JASNYM NIEBIESKIM TLE (#E8EEF4)");
        System.out.println("   - DOSKONALE WIDOCZNE I CZYTELNE!");
        System.out.println("\n❌ JEŚLI WIDZISZ:");
        System.out.println("   - Biały tekst na ciemnym tle");
        System.out.println("   - Tekst jest nieczytelny");
        System.out.println("   - TO ZNACZY ŻE STYLE NIE DZIAŁAJĄ!");
        System.out.println("=".repeat(80) + "\n");
    }
}

