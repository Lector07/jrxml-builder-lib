package pl.lib.automation;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test wizualny do weryfikacji formatowania nagłówków tabel w PDF
 */
class HeaderFormattingVisualTest {

    @Test
    void shouldFormatTableHeadersInPdf() throws Exception {
        String json = """
                {
                    "informacje": {
                        "opis": "Test formatowania nagłówków tabel"
                    },
                    "dane_snake_case": [
                        {"user_name": "Jan Kowalski", "email_address": "jan@example.com", "phone_number": "123456789", "account_status": "Aktywny"},
                        {"user_name": "Anna Nowak", "email_address": "anna@example.com", "phone_number": "987654321", "account_status": "Aktywny"},
                        {"user_name": "Piotr Wiśniewski", "email_address": "piotr@example.com", "phone_number": "555666777", "account_status": "Nieaktywny"}
                    ],
                    "dane_camelCase": [
                        {"firstName": "Maria", "lastName": "Kowalczyk", "dateOfBirth": "1990-05-15", "cityName": "Warszawa"},
                        {"firstName": "Tomasz", "lastName": "Lewandowski", "dateOfBirth": "1985-03-22", "cityName": "Kraków"},
                        {"firstName": "Katarzyna", "lastName": "Kamińska", "dateOfBirth": "1992-11-08", "cityName": "Gdańsk"}
                    ],
                    "dane_zagniedzone": [
                        {"company_user_firstName": "Adam", "company_user_lastName": "Zieliński", "company_department_name": "IT"},
                        {"company_user_firstName": "Ewa", "company_user_lastName": "Szymańska", "company_department_name": "HR"},
                        {"company_user_firstName": "Marek", "company_user_lastName": "Dąbrowski", "company_department_name": "Finanse"}
                    ],
                    "podsumowanie": {
                        "total_count": "9",
                        "generation_date": "2025-11-21"
                    }
                }
                """;

        JsonReportGenerator generator = new JsonReportGenerator();
        JasperPrint jasperPrint = generator.generateReport(json, "Test Formatowania Nagłówków");

        assertThat(jasperPrint).isNotNull();
        assertThat(jasperPrint.getPages()).isNotEmpty();

        String outputPath = "target/test-output/raport_formatowanie_naglowkow.pdf";
        File outputDir = new File("target/test-output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        File pdfFile = new File(outputPath);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ PDF z formatowanymi nagłówkami wygenerowany!");
        System.out.println("=".repeat(80));
        System.out.println("📁 Ścieżka: " + pdfFile.getAbsolutePath());
        System.out.println("📊 Rozmiar: " + pdfFile.length() + " bajtów");
        System.out.println("📄 Liczba stron: " + jasperPrint.getPages().size());
        System.out.println("\n🔍 WERYFIKACJA RĘCZNA:");
        System.out.println("   Otwórz PDF i sprawdź nagłówki tabel:");
        System.out.println("\n   📋 Tabela 1 (snake_case) - nagłówki powinny być:");
        System.out.println("      ❌ NIE: user_name | email_address | phone_number | account_status");
        System.out.println("      ✅ TAK: Name | Address | Number | Status");
        System.out.println("\n   📋 Tabela 2 (camelCase) - nagłówki powinny być:");
        System.out.println("      ❌ NIE: firstName | lastName | dateOfBirth | cityName");
        System.out.println("      ✅ TAK: First Name | Last Name | Date Of Birth | City Name");
        System.out.println("\n   📋 Tabela 3 (zagnieżdżone) - nagłówki powinny być:");
        System.out.println("      ❌ NIE: company_user_firstName | company_user_lastName | company_department_name");
        System.out.println("      ✅ TAK: First Name | Last Name | Name");
        System.out.println("=".repeat(80) + "\n");
    }
}

