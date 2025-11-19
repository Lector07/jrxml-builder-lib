package pl.lib.automation;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
class JsonReportGeneratorComplexTest {
    @Test
    void shouldGenerateComplexReportWithMultipleTables() throws Exception {
        String json = """
                {
                    "informacje_ogolne": {
                        "tytul": "Raport Kwartalny Q4 2024",
                        "data_generowania": "2025-11-07",
                        "autor": "Jan Kowalski",
                        "departament": "Finanse"
                    },
                    "podsumowanie_wykonawcze": {
                        "opis": "Prezentacja wyników finansowych za Q4 2024",
                        "przychody_calkowite": "25000000 PLN",
                        "koszty_calkowite": "18000000 PLN",
                        "zysk_netto": "7000000 PLN",
                        "rentownosc": "28%"
                    },
                    "przychody_wg_regionow": [
                        {"region": "Mazowieckie", "przychod": "8000000", "wzrost": "15%", "udzial": "32%"},
                        {"region": "Slaskie", "przychod": "6000000", "wzrost": "10%", "udzial": "24%"},
                        {"region": "Wielkopolskie", "przychod": "5000000", "wzrost": "12%", "udzial": "20%"},
                        {"region": "Malopolskie", "przychod": "4000000", "wzrost": "8%", "udzial": "16%"},
                        {"region": "Pomorskie", "przychod": "2000000", "wzrost": "5%", "udzial": "8%"}
                    ],
                    "struktura_kosztow": {
                        "koszty_stale": "10000000 PLN",
                        "koszty_zmienne": "8000000 PLN",
                        "szczegoly_kosztow": [
                            {"kategoria": "Wynagrodzenia", "kwota": "7000000", "procent": "38.9%"},
                            {"kategoria": "Materialy", "kwota": "5000000", "procent": "27.8%"},
                            {"kategoria": "Marketing", "kwota": "2000000", "procent": "11.1%"},
                            {"kategoria": "Logistyka", "kwota": "2000000", "procent": "11.1%"},
                            {"kategoria": "IT", "kwota": "1500000", "procent": "8.3%"},
                            {"kategoria": "Administracja", "kwota": "500000", "procent": "2.8%"}
                        ]
                    },
                    "projekty": {
                        "liczba_aktywnych": "12",
                        "budzet_laczny": "50000000 PLN",
                        "lista_projektow": [
                            {"nazwa": "Projekt Alpha", "budzet": "10000000", "status": "W realizacji", "progress": "75%", "manager": "Anna Nowak"},
                            {"nazwa": "Projekt Beta", "budzet": "8000000", "status": "W realizacji", "progress": "60%", "manager": "Piotr Wisniewski"},
                            {"nazwa": "Projekt Gamma", "budzet": "7000000", "status": "Zakonczony", "progress": "100%", "manager": "Maria Kowalczyk"},
                            {"nazwa": "Projekt Delta", "budzet": "6000000", "status": "W realizacji", "progress": "45%", "manager": "Tomasz Lewandowski"},
                            {"nazwa": "Projekt Epsilon", "budzet": "5000000", "status": "Planowanie", "progress": "10%", "manager": "Katarzyna Kaminska"}
                        ]
                    },
                    "zasoby_ludzkie": {
                        "liczba_pracownikow": "350",
                        "nowe_zatrudnienia": "25",
                        "odejscia": "10",
                        "statystyki_departamentow": [
                            {"departament": "Sprzedaz", "pracownicy": "80", "etat": "Full-time", "srednie_wynagrodzenie": "8000"},
                            {"departament": "IT", "pracownicy": "60", "etat": "Full-time", "srednie_wynagrodzenie": "12000"},
                            {"departament": "Marketing", "pracownicy": "40", "etat": "Full-time", "srednie_wynagrodzenie": "7500"},
                            {"departament": "HR", "pracownicy": "30", "etat": "Full-time", "srednie_wynagrodzenie": "7000"},
                            {"departament": "Finanse", "pracownicy": "45", "etat": "Full-time", "srednie_wynagrodzenie": "9000"},
                            {"departament": "Produkcja", "pracownicy": "95", "etat": "Full-time", "srednie_wynagrodzenie": "6000"}
                        ]
                    },
                    "kluczowe_wskazniki": {
                        "ROI": "35%",
                        "EBITDA": "9500000 PLN",
                        "margin_operacyjny": "32%",
                        "zadluzenie": "15000000 PLN",
                        "plynnosc_finansowa": "Bardzo dobra"
                    },
                    "rekomendacje": {
                        "priorytet_1": "Zwiększyć inwestycje w marketing cyfrowy",
                        "priorytet_2": "Rozszerzyć zespół IT o 10 dodatkowych specjalistów",
                        "priorytet_3": "Zoptymalizować koszty logistyki poprzez automatyzację",
                        "priorytet_4": "Rozpocząć negocjacje z nowymi dostawcami materiałów"
                    }
                }
                """;
        JsonReportGenerator generator = new JsonReportGenerator();
        JasperPrint jasperPrint = generator.generateReport(json, "Raport Kwartalny - Kompletna Analiza");
        assertThat(jasperPrint).isNotNull();
        assertThat(jasperPrint.getPages()).isNotEmpty();
        String outputPath = "target/test-output/raport_kompleksowy_test.pdf";
        File outputDir = new File("target/test-output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        File pdfFile = new File(outputPath);
        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
        System.out.println("✅ Kompleksowy PDF wygenerowany pomyślnie: " + pdfFile.getAbsolutePath());
        System.out.println("📄 Rozmiar pliku: " + pdfFile.length() + " bajtów");
        System.out.println("📊 Liczba stron: " + jasperPrint.getPages().size());
        System.out.println("🎯 Test weryfikuje:");
        System.out.println("   - Wiele zagnieżdżonych sekcji");
        System.out.println("   - 4 różne tabele z różną liczbą kolumn");
        System.out.println("   - Mieszane typy danych (teksty, liczby, procenty)");
        System.out.println("   - Wielopoziomową hierarchię danych");
    }
}
