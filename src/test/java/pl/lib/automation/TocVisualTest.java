package pl.lib.automation;
import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.Test;
import pl.lib.config.ReportConfig;
import pl.lib.model.CompanyInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
class TocVisualTest {
    @Test
    void generateReportWithTocForManualInspection() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade(true);
        String jsonContent = """
            {
              "wprowadzenie": {
                "opis": "To jest raport testowy do weryfikacji spisu treści",
                "autor": "System testowy",
                "data": "2025-11-17"
              },
              "sekcja_pierwsza": {
                "tytul": "Analiza finansowa",
                "podsekcja_a": {
                  "nazwa": "Przychody",
                  "wartosc": "1 000 000 PLN",
                  "wzrost": "15%"
                },
                "podsekcja_b": {
                  "nazwa": "Koszty",
                  "wartosc": "750 000 PLN",
                  "spadek": "5%"
                },
                "dane_tabelaryczne": [
                  {"miesiac": "Styczeń", "przychod": 80000, "koszt": 60000},
                  {"miesiac": "Luty", "przychod": 85000, "koszt": 62000},
                  {"miesiac": "Marzec", "przychod": 90000, "koszt": 63000}
                ]
              },
              "sekcja_druga": {
                "tytul": "Zasoby ludzkie",
                "podsekcja_a": {
                  "liczba_pracownikow": 150,
                  "nowe_zatrudnienia": 10
                },
                "pracownicy": [
                  {"imie": "Jan Kowalski", "stanowisko": "Manager", "staz": "5 lat"},
                  {"imie": "Anna Nowak", "stanowisko": "Developer", "staz": "3 lata"},
                  {"imie": "Piotr Wiśniewski", "stanowisko": "Tester", "staz": "2 lata"}
                ]
              },
              "sekcja_trzecia": {
                "tytul": "Podsumowanie i wnioski",
                "wniosek_1": "Firma rozwija się dynamicznie",
                "wniosek_2": "Należy zwiększyć budżet marketingowy",
                "wniosek_3": "Planowane zatrudnienie 20 nowych osób"
              }
            }
            """;
        CompanyInfo companyInfo = CompanyInfo.builder("TechCorp Sp. z o.o.")
                .address("ul. Innowacyjna 123")
                .location("00-001", "Warszawa")
                .build();
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Roczny 2025 - Test Spisu Treści")
                .companyInfo(companyInfo)
                .pageFormat("A4")
                .withPageFooterEnabled(true)
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        File outputFile = new File("target/test-output/raport_test_spisu_tresci.pdf");
        outputFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✓ RAPORT WYGENEROWANY POMYŚLNIE!");
        System.out.println("=".repeat(80));
        System.out.println("📄 Lokalizacja: " + outputFile.getAbsolutePath());
        System.out.println("📊 Rozmiar: " + String.format("%.2f KB", pdfBytes.length / 1024.0));
        System.out.println("\n🔍 INSTRUKCJE WERYFIKACJI:");
        System.out.println("   1. Otwórz plik PDF w przeglądarce");
        System.out.println("   2. Sprawdź stronę 2 - powinien być tam Spis Treści");
        System.out.println("   3. Sprawdź, czy numery stron w spisie są poprawne");
        System.out.println("   4. Kliknij na wpisy w spisie treści - powinny przenosić na właściwe strony");
        System.out.println("   5. Sprawdź panel zakładek (bookmarks) w czytniku PDF");
        System.out.println("=".repeat(80) + "\n");
    }
    @Test
    void generateLargeReportWithMultipleSections() throws JRException, IOException {
        AutomatedReportFacade facade = new AutomatedReportFacade();
        String jsonContent = """
            {
              "rozdzial_1": {
                "tytul": "Wprowadzenie do analizy",
                "sekcja_1_1": {
                  "temat": "Cele badania",
                  "opis": "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                },
                "sekcja_1_2": {
                  "temat": "Metodologia",
                  "opis": "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                }
              },
              "rozdzial_2": {
                "tytul": "Dane statystyczne",
                "tabela_glowna": [
                  {"kategoria": "A", "q1": 100, "q2": 120, "q3": 140, "q4": 160},
                  {"kategoria": "B", "q1": 200, "q2": 220, "q3": 240, "q4": 260},
                  {"kategoria": "C", "q1": 300, "q2": 320, "q3": 340, "q4": 360},
                  {"kategoria": "D", "q1": 400, "q2": 420, "q3": 440, "q4": 460},
                  {"kategoria": "E", "q1": 500, "q2": 520, "q3": 540, "q4": 560}
                ]
              },
              "rozdzial_3": {
                "tytul": "Analiza wyników",
                "sekcja_3_1": {
                  "podtytul": "Wyniki pozytywne",
                  "szczegoly": {
                    "wzrost": "15%",
                    "efektywnosc": "92%"
                  }
                },
                "sekcja_3_2": {
                  "podtytul": "Obszary do poprawy",
                  "szczegoly": {
                    "spadek": "3%",
                    "wydajnosc": "78%"
                  }
                }
              },
              "rozdzial_4": {
                "tytul": "Rekomendacje",
                "rekomendacja_1": "Zwiększyć inwestycje w marketing",
                "rekomendacja_2": "Poprawić obsługę klienta",
                "rekomendacja_3": "Rozszerzyć ofertę produktową"
              }
            }
            """;
        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Kompleksowy - Wielosekcyjny")
                .pageFormat("A4")
                .build();
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        File outputFile = new File("target/test-output/raport_wielosekcyjny_toc.pdf");
        outputFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }
        System.out.println("\n✓ Raport wielosekcyjny zapisany: " + outputFile.getAbsolutePath());
        System.out.println("  Rozmiar: " + String.format("%.2f KB", pdfBytes.length / 1024.0));
    }
}
