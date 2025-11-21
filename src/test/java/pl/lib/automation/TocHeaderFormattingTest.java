package pl.lib.automation;

import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.Test;
import pl.lib.config.ReportConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Test do weryfikacji formatowania nagłówków w AutomatedReportFacade
 */
class TocHeaderFormattingTest {

    @Test
    void shouldFormatHeadersInTocReport() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();

        String jsonContent = """
            {
              "informacje_podstawowe": {
                "nazwa_projektu": "System ERP",
                "data_rozpoczecia": "2024-01-15",
                "budzet_poczatkowy": "500000 PLN"
              },
              "zespol_projektowy": [
                {"user_name": "Jan Kowalski", "email_address": "jan@example.com", "phone_number": "+48 123 456 789", "account_status": "Aktywny"},
                {"user_name": "Anna Nowak", "email_address": "anna@example.com", "phone_number": "+48 987 654 321", "account_status": "Aktywny"},
                {"user_name": "Piotr Wiśniewski", "email_address": "piotr@example.com", "phone_number": "+48 555 666 777", "account_status": "Nieaktywny"}
              ],
              "harmonogram_projektu": [
                {"task_name": "Analiza wymagań", "start_date": "2024-01-15", "end_date": "2024-02-15", "completion_percentage": "100%"},
                {"task_name": "Projektowanie architektury", "start_date": "2024-02-16", "end_date": "2024-03-30", "completion_percentage": "100%"},
                {"task_name": "Implementacja modułu finansowego", "start_date": "2024-04-01", "end_date": "2024-06-30", "completion_percentage": "80%"},
                {"task_name": "Testy integracyjne", "start_date": "2024-07-01", "end_date": "2024-08-15", "completion_percentage": "45%"}
              ],
              "metryki_wydajnosci": [
                {"metric_name": "Czas odpowiedzi systemu", "current_value": "125ms", "target_value": "100ms", "status_indicator": "Warning"},
                {"metric_name": "Dostępność systemu", "current_value": "99.8%", "target_value": "99.9%", "status_indicator": "Good"},
                {"metric_name": "Liczba użytkowników jednocześnie", "current_value": "450", "target_value": "500", "status_indicator": "Good"}
              ]
            }
            """;

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Projektu ERP - Test Formatowania Nagłówków")
                .pageFormat("A4")
                .withPageFooterEnabled(true)
                .build();

        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);

        File outputFile = new File("target/test-output/raport_toc_header_formatting.pdf");
        outputFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ PDF z formatowaniem nagłówków w TOC wygenerowany!");
        System.out.println("=".repeat(80));
        System.out.println("📁 Ścieżka: " + outputFile.getAbsolutePath());
        System.out.println("📊 Rozmiar: " + String.format("%.2f KB", pdfBytes.length / 1024.0));

        System.out.println("\n🔍 WERYFIKACJA - sprawdź nagłówki w tabelach:");
        System.out.println("\n   📋 Tabela 'zespol_projektowy' powinny mieć nagłówki:");
        System.out.println("      ✅ Name | Address | Number | Status");
        System.out.println("      ❌ NIE: user_name | email_address | phone_number | account_status");

        System.out.println("\n   📋 Tabela 'harmonogram_projektu' powinny mieć nagłówki:");
        System.out.println("      ✅ Name | Date | Date | Percentage");
        System.out.println("      ❌ NIE: task_name | start_date | end_date | completion_percentage");

        System.out.println("\n   📋 Tabela 'metryki_wydajnosci' powinny mieć nagłówki:");
        System.out.println("      ✅ Name | Value | Value | Indicator");
        System.out.println("      ❌ NIE: metric_name | current_value | target_value | status_indicator");

        System.out.println("=".repeat(80) + "\n");
    }
}

