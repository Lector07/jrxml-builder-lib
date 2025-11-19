package pl.lib.automation;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
class JsonReportGeneratorRefactoredTest {
    @Test
    void shouldGenerateReportWithRefactoredCode() throws Exception {
        String json = """
                {
                    "projekt": {
                        "nazwa": "Budowa mostu",
                        "lokalizacja": "Warszawa",
                        "budzet": "15000000",
                        "koszty": [
                            {"kategoria": "Materialy", "kwota": "5000000", "status": "Zaplanowane"},
                            {"kategoria": "Robocizna", "kwota": "6000000", "status": "W trakcie"},
                            {"kategoria": "Sprzet", "kwota": "3000000", "status": "Zaplanowane"},
                            {"kategoria": "Inne", "kwota": "1000000", "status": "Zaplanowane"}
                        ]
                    },
                    "wykonawca": {
                        "firma": "BudowaXYZ Sp. z o.o.",
                        "adres": "ul. Budowlana 123, Warszawa",
                        "telefon": "+48 123 456 789",
                        "email": "kontakt@budowaxyz.pl"
                    },
                    "harmonogram": [
                        {"etap": "Projekt", "start": "2024-01-01", "koniec": "2024-03-31", "postep": "100%"},
                        {"etap": "Przygotowanie terenu", "start": "2024-04-01", "koniec": "2024-05-31", "postep": "100%"},
                        {"etap": "Fundamenty", "start": "2024-06-01", "koniec": "2024-08-31", "postep": "80%"},
                        {"etap": "Konstrukcja", "start": "2024-09-01", "koniec": "2024-12-31", "postep": "30%"}
                    ],
                    "podsumowanie": {
                        "calkowity_koszt": "15000000",
                        "wydano": "8500000",
                        "pozostalo": "6500000",
                        "procent_realizacji": "56.67%"
                    }
                }
                """;
        JsonReportGenerator generator = new JsonReportGenerator();
        JasperPrint jasperPrint = generator.generateReport(json, "Raport Projektu Budowlanego");
        assertThat(jasperPrint).isNotNull();
        assertThat(jasperPrint.getPages()).isNotEmpty();
        String outputPath = "target/test-output/raport_refaktoryzacja_test.pdf";
        File outputDir = new File("target/test-output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        File pdfFile = new File(outputPath);
        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
        System.out.println("✅ PDF wygenerowany pomyślnie: " + pdfFile.getAbsolutePath());
        System.out.println("📄 Rozmiar pliku: " + pdfFile.length() + " bajtów");
        System.out.println("📊 Liczba stron: " + jasperPrint.getPages().size());
    }
}
