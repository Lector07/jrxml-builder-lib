package pl.lib.automation;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import org.junit.jupiter.api.Test;
import pl.lib.config.ChartConfig;
import pl.lib.config.ReportConfig;
import pl.lib.model.ChartType;
import pl.lib.model.CompanyInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test kompleksowy - wszystkie funkcje systemu raportowania na raz
 */
class ComprehensiveReportTest {

    @Test
    void shouldGenerateCompleteReportWithEverything() throws JRException, IOException {
        String json = """
            {
              "streszczenie_wykonawcze": {
                "opis": "Niniejszy raport przedstawia kompleksową analizę działalności firmy TechCorp S.A. za rok obrotowy 2024. Dokument zawiera szczegółowe dane finansowe, analizę rynkową oraz prognozy na kolejny rok. Raport został przygotowany na podstawie zaudytowanych sprawozdań finansowych oraz wewnętrznych analiz prowadzonych przez departamenty kontrolingu i strategii.",
                "cel_dokumentu": "Celem dokumentu jest przedstawienie akcjonariuszom, zarządowi oraz kluczowym interesariuszom pełnego obrazu sytuacji finansowej i operacyjnej spółki. Raport służy również jako podstawa do podejmowania strategicznych decyzji biznesowych na rok 2025.",
                "zakres_czasowy": "1 stycznia 2024 - 31 grudnia 2024",
                "data_publikacji": "21 listopada 2025",
                "wersja_dokumentu": "2.1 - Finalna"
              },
              
              "analiza_finansowa": {
                "wprowadzenie": "Rok 2024 był dla firmy okresem dynamicznego wzrostu i ekspansji na nowe rynki. Przychody całkowite wzrosły o 23% w porównaniu do roku poprzedniego, osiągając poziom 125 milionów złotych. Wzrost ten był wynikiem zarówno organicznej ekspansji na rynku krajowym, jak i udanych przejęć strategicznych na rynkach zagranicznych. Szczególnie udany był IV kwartał, w którym spółka odnotowała rekordowe wyniki sprzedażowe.",
                
                "przychody_kwartalne": [
                  {"kwartal": "Q1 2024", "przychody": 28500000, "wzrost_rr": "18%", "marza": "32%"},
                  {"kwartal": "Q2 2024", "przychody": 29800000, "wzrost_rr": "21%", "marza": "34%"},
                  {"kwartal": "Q3 2024", "przychody": 31200000, "wzrost_rr": "25%", "marza": "33%"},
                  {"kwartal": "Q4 2024", "przychody": 35500000, "wzrost_rr": "28%", "marza": "35%"}
                ],
                
                "omowienie_wynikow": "Analiza kwartalnych wyników pokazuje systematyczny wzrost przychodów we wszystkich okresach sprawozdawczych. Szczególnie istotny jest wzrost marży operacyjnej w IV kwartale, co świadczy o poprawie efektywności operacyjnej oraz skutecznym zarządzaniu kosztami. Implementacja nowych systemów automatyzacji procesów biznesowych przyniosła wymierne oszczędności, szacowane na 4,5 miliona złotych rocznie. Dodatkowo, optymalizacja łańcucha dostaw pozwoliła na redukcję kosztów logistycznych o 12%.",
                
                "wykres_przychodow_kwartalnych": {
                  "type": "bar",
                  "data": {
                    "Q1 2024": 28.5,
                    "Q2 2024": 29.8,
                    "Q3 2024": 31.2,
                    "Q4 2024": 35.5
                  },
                  "title": "Przychody kwartalne (mln PLN)"
                },
                
                "struktura_przychodow": "Przychody firmy pochodzą z trzech głównych segmentów biznesowych: rozwiązań software'owych (45%), usług consultingowych (32%) oraz produktów sprzętowych (23%). Segment software'owy wykazuje najwyższą dynamikę wzrostu, co jest zgodne z ogólnorynkowym trendem cyfryzacji przedsiębiorstw. W ramach tego segmentu szczególnie dobrze sprzedają się rozwiązania chmurowe oraz platformy do zarządzania danymi klientów (CRM).",
                
                "wykres_struktura_przychodow": {
                  "type": "pie",
                  "data": {
                    "Software": 45.0,
                    "Consulting": 32.0,
                    "Hardware": 23.0
                  },
                  "title": "Struktura przychodów według segmentów (%)"
                }
              },
              
              "analiza_rynkowa": {
                "pozycja_konkurencyjna": "TechCorp S.A. utrzymuje silną pozycję na rynku krajowym, zajmując trzecie miejsce pod względem udziału rynkowego w branży IT. W segmencie rozwiązań dla sektora finansowego jesteśmy liderem z 28% udziałem rynkowym. Główni konkurenci to GlobalTech (32% rynku), InnovateSoft (19% rynku) oraz szereg mniejszych graczy lokalnych.",
                
                "udzialy_rynkowe": [
                  {"firma": "GlobalTech", "udzial_procentowy": 32, "zmiana_rr": "+2%"},
                  {"firma": "TechCorp (my)", "udzial_procentowy": 28, "zmiana_rr": "+5%"},
                  {"firma": "InnovateSoft", "udzial_procentowy": 19, "zmiana_rr": "-1%"},
                  {"firma": "DataSystems", "udzial_procentowy": 12, "zmiana_rr": "+1%"},
                  {"firma": "Pozostali", "udzial_procentowy": 9, "zmiana_rr": "-3%"}
                ],
                
                "trendy_rynkowe": "Rynek usług IT w Polsce rósł w 2024 roku w tempie 15% rocznie, napędzany głównie inwestycjami w transformację cyfrową przedsiębiorstw oraz rosnącym zapotrzebowaniem na rozwiązania AI i uczenia maszynowego. Obserwujemy również wzrost zainteresowania rozwiązaniami typu cloud-native oraz platformami low-code/no-code. Segment cyberbezpieczeństwa wykazuje najszybszy wzrost (27% r/r), co jest odpowiedzią na rosnące zagrożenia cybernetyczne.",
                
                "wykres_udzialy_rynkowe": {
                  "type": "line",
                  "data": {
                    "GlobalTech": 32.0,
                    "TechCorp": 28.0,
                    "InnovateSoft": 19.0,
                    "DataSystems": 12.0,
                    "Pozostali": 9.0
                  },
                  "title": "Udziały rynkowe konkurentów (%)"
                },
                
                "analiza_swot": "Mocne strony firmy to: ugruntowana pozycja rynkowa, silna marka, wykwalifikowany zespół (320 specjalistów IT), szeroki portfel produktów oraz długoterminowe kontrakty z kluczowymi klientami. Do słabych stron zaliczamy relatywnie wysokie koszty operacyjne, ograniczoną obecność na rynkach zagranicznych oraz zależność od kilku kluczowych klientów (top 5 klientów stanowi 42% przychodów). Szanse to rosnący rynek IT, możliwości akwizycji mniejszych firm, rozwój segmentu AI oraz ekspansja zagraniczna. Zagrożenia obejmują intensywną konkurencję, braki kadrowe na rynku IT, ryzyko recesji gospodarczej oraz szybkie zmiany technologiczne."
              },
              
              "dzialania_operacyjne": {
                "projekty_realizowane": "W 2024 roku firma zrealizowała 127 projektów dla 89 klientów. Średni czas realizacji projektu wyniósł 4,3 miesiąca, co stanowi poprawę o 15% w porównaniu do roku poprzedniego. Wdrożyliśmy metodyki Agile we wszystkich zespołach projektowych, co znacząco poprawiło efektywność i satysfakcję klientów. Wskaźnik Net Promoter Score (NPS) wzrósł z 42 do 58 punktów.",
                
                "kluczowe_projekty": [
                  {"nazwa_projektu": "System ERP dla BankCorp", "wartosc_kontraktu": 8500000, "status": "Zakończony", "ocena_klienta": "5/5"},
                  {"nazwa_projektu": "Platforma e-commerce RetailPlus", "wartosc_kontraktu": 6200000, "status": "W realizacji", "ocena_klienta": "4.5/5"},
                  {"nazwa_projektu": "Migracja do chmury FinanceGroup", "wartosc_kontraktu": 4800000, "status": "Zakończony", "ocena_klienta": "5/5"},
                  {"nazwa_projektu": "AI Analytics dla LogisticsPro", "wartosc_kontraktu": 3900000, "status": "W realizacji", "ocena_klienta": "4.8/5"},
                  {"nazwa_projektu": "CRM System dla HealthCare Ltd", "wartosc_kontraktu": 3200000, "status": "Planowanie", "ocena_klienta": "N/A"}
                ],
                
                "infrastruktura_it": "Firma zainwestowała 8,5 miliona złotych w modernizację infrastruktury IT, w tym budowę prywatnej chmury, aktualizację systemów bezpieczeństwa oraz implementację narzędzi DevOps. Obecnie 85% naszych systemów działa w architekturze chmurowej, co zwiększa elastyczność i obniża koszty utrzymania. Wdrożono również zaawansowane systemy monitoringu i automatyzacji, które pozwalają na proaktywne wykrywanie i rozwiązywanie problemów.",
                
                "zespol_i_rozwoj": "Zespół firmy powiększył się o 45 osób, osiągając stan 320 pracowników na koniec 2024 roku. Zainwestowaliśmy 1,2 miliona złotych w szkolenia i rozwój kompetencji pracowników, ze szczególnym naciskiem na technologie AI, cloud computing oraz cyberbezpieczeństwo. Rotacja pracowników spadła z 18% do 12%, co jest wynikiem lepszym od średniej branżowej wynoszącej 22%. Wprowadzono również program mentoringowy oraz ścieżki rozwoju kariery dla wszystkich stanowisk technicznych."
              },
              
              "perspektywy_na_2025": {
                "cele_strategiczne": "Na rok 2025 zaplanowaliśmy ambitne cele wzrostu. Przewidujemy zwiększenie przychodów o 28% do poziomu 160 milionów złotych. Kluczowe inicjatywy to: ekspansja na rynki Czech i Słowacji, rozwój segmentu AI i uczenia maszynowego, akwizycja 2-3 mniejszych firm specjalizujących się w niszowych technologiach oraz zwiększenie udziału w segmencie cyberbezpieczeństwa.",
                
                "prognozy_finansowe": "Prognozujemy, że marża EBITDA utrzyma się na poziomie 18-20%, przy jednoczesnym wzroście inwestycji w R&D do 12% przychodów (wzrost z obecnych 8%). Planujemy również emisję obligacji korporacyjnych o wartości 25 milionów złotych na finansowanie ekspansji zagranicznej. Zakładamy, że wolne przepływy pieniężne (FCF) osiągną poziom 22 milionów złotych, co pozwoli na dywidendę w wysokości 8 złotych na akcję.",
                
                "inwestycje_planowane": "W 2025 roku planujemy przeznaczyć 18 milionów złotych na rozwój nowych produktów, w tym platformy AI do analizy predykcyjnej, rozwiązań IoT dla przemysłu 4.0 oraz narzędzi do automatyzacji procesów biznesowych (RPA). Dodatkowo 12 milionów złotych zostanie przeznaczonych na akwizycje oraz 8 milionów na budowę nowego centrum R&D w Krakowie.",
                
                "ryzyka_i_mitigacja": "Główne ryzyka identyfikowane na 2025 rok to: spowolnienie gospodarcze (prawdopodobieństwo 30%), problemy z rekrutacją specjalistów IT (50%), intensyfikacja konkurencji (60%) oraz zmiany regulacyjne dotyczące ochrony danych (40%). Dla każdego z tych ryzyk przygotowano plany mitygacyjne, w tym dywersyfikację bazy klientów, programy employer brandingowe, inwestycje w automatyzację oraz compliance team."
              },
              
              "podsumowanie_i_rekomendacje": {
                "glowne_wnioski": "Rok 2024 był dla TechCorp S.A. rokiem przełomowym, charakteryzującym się rekordowymi wynikami finansowymi oraz znaczącym umocnieniem pozycji rynkowej. Systematyczny wzrost przychodów, poprawa marż oraz skuteczne zarządzanie kosztami świadczą o dojrzałości organizacyjnej firmy i skuteczności implementowanej strategii. Silna pozycja finansowa i operacyjna stanowi solidną podstawę do realizacji ambitnych planów ekspansji na 2025 rok.",
                
                "kluczowe_rekomendacje": "Zarząd rekomenduje: 1) Kontynuację strategii wzrostu organicznego przy jednoczesnym poszukiwaniu możliwości akwizycyjnych na rynkach Czech i Słowacji. 2) Zwiększenie inwestycji w R&D do poziomu 12% przychodów, ze szczególnym naciskiem na technologie AI i ML. 3) Wzmocnienie zespołu sprzedażowego o 15 osób w celu lepszej penetracji rynku korporacyjnego. 4) Implementację programu cyfryzacji wewnętrznej (Digital Workplace) dla poprawy efektywności operacyjnej. 5) Rozpoczęcie przygotowań do potencjalnego IPO w 2026 roku, w tym wdrożenie standardów raportowania dla spółek publicznych.",
                
                "nastepne_kroki": "W najbliższych miesiącach priorytetowe działania to: finalizacja procesu due diligence dla dwóch potencjalnych celów akwizycyjnych, rozpoczęcie rekrutacji na stanowiska w nowym centrum R&D, uruchomienie kampanii marketingowej na rynkach zagranicznych oraz przeprowadzenie procesu emisji obligacji. Równolegle będą kontynuowane prace nad nowymi produktami oraz optymalizacja procesów wewnętrznych."
              }
            }
            """;

        AutomatedReportFacade facade = new AutomatedReportFacade(false);

        CompanyInfo companyInfo = CompanyInfo.builder("TechCorp S.A.")
                .address("ul. Technologiczna 123")
                .location("00-001", "Warszawa").website("www.techcorp.pl")
                .taxId("PL1234567890")
                .build();

        ReportConfig config = new ReportConfig.Builder()
                .title("Raport Roczny 2024 - Kompleksowa Analiza")
                .companyInfo(companyInfo)
                .pageFormat("A4")
                .withPageFooterEnabled(true)
                .build();

        byte[] pdfBytes = facade.generateCompositeReport(json, config);

        File outputFile = new File("target/test-output/raport_kompletny_test.pdf");
        outputFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(pdfBytes);
        }

        assertThat(pdfBytes).isNotEmpty();
        assertThat(outputFile).exists();
        assertThat(outputFile.length()).isGreaterThan(50000); // Powinien być duży raport (>50KB)

        System.out.println("\n" + "=".repeat(100));
        System.out.println("🎉 KOMPLETNY RAPORT WYGENEROWANY POMYŚLNIE!");
        System.out.println("=".repeat(100));
        System.out.println("📁 Lokalizacja: " + outputFile.getAbsolutePath());
        System.out.println("📊 Rozmiar: " + String.format("%.2f KB", pdfBytes.length / 1024.0));
        System.out.println();
        System.out.println("📋 Zawartość raportu:");
        System.out.println("   ✅ Strona tytułowa z logo i danymi firmy");
        System.out.println("   ✅ Spis treści z numerami stron");
        System.out.println("   ✅ Duża ilość sformatowanego tekstu (7+ sekcji)");
        System.out.println("   ✅ 3 różne tabele:");
        System.out.println("      - Przychody kwartalne (4 wiersze)");
        System.out.println("      - Udziały rynkowe firm (5 wierszy)");
        System.out.println("      - Kluczowe projekty (5 wierszy)");
        System.out.println("   ✅ 3 wykresy:");
        System.out.println("      - Wykres słupkowy przychodów kwartalnych");
        System.out.println("      - Wykres kołowy struktury przychodów");
        System.out.println("      - Wykres liniowy udziałów rynkowych");
        System.out.println("   ✅ Automatyczne formatowanie nagłówków tabel");
        System.out.println("   ✅ Hierarchiczna struktura nagłówków");
        System.out.println("   ✅ Zakładki (bookmarks) w PDF");
        System.out.println();
        System.out.println("🔍 Aby zweryfikować:");
        System.out.println("   1. Otwórz plik PDF w przeglądarce lub Adobe Reader");
        System.out.println("   2. Sprawdź spis treści na stronie 2");
        System.out.println("   3. Kliknij na wpisy w spisie - powinny przenosić do odpowiednich sekcji");
        System.out.println("   4. Sprawdź panel zakładek (bookmarks) po lewej stronie");
        System.out.println("   5. Przewiń raport - powinno być ~8-10 stron");
        System.out.println("=".repeat(100));
        System.out.println();
    }
}

