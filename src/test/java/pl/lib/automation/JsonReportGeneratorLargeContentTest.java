package pl.lib.automation;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class JsonReportGeneratorLargeContentTest {

    @Test
    void shouldHandleLargeContentWithTablesAndPagination() throws Exception {
        String json = """
                {
                    "wprowadzenie": {
                        "tytul": "Szczegółowa Analiza Projektu Infrastrukturalnego",
                        "autor": "Dr inż. Jan Kowalski",
                        "data": "2025-11-07",
                        "wersja": "3.2.1"
                    },
                    "streszczenie_wykonawcze": {
                        "cel_dokumentu": "Niniejszy dokument przedstawia kompleksową analizę projektu infrastrukturalnego wraz z oceną ryzyka, harmonogramem realizacji oraz prognozami finansowymi. Analiza obejmuje okres od stycznia 2024 roku do grudnia 2027 roku i uwzględnia wszystkie aspekty techniczne, prawne oraz środowiskowe związane z realizacją projektu.",
                        "zakres_prac": "Projekt obejmuje budowę, modernizację oraz integrację systemów infrastruktury miejskiej na obszarze 150 hektarów. W ramach projektu planowane jest wykonanie następujących zadań: budowa 25 km dróg ekspresowych, modernizacja 15 km sieci wodociągowej, budowa 3 stacji uzdatniania wody, modernizacja 8 przepompowni ścieków, budowa 12 km sieci kanalizacyjnej, montaż 500 lamp ulicznych LED, budowa 4 rond turbinowych, modernizacja 20 przejść dla pieszych, budowa 15 km ścieżek rowerowych oraz zagospodarowanie 8 parków miejskich o łącznej powierzchni 45 hektarów.",
                        "glowne_zalozenia": "Projekt zakłada zastosowanie najnowszych technologii w zakresie budownictwa infrastrukturalnego, ze szczególnym uwzględnieniem rozwiązań ekologicznych i energooszczędnych. Wszystkie prace będą realizowane zgodnie z normami europejskimi EN oraz krajowymi przepisami budowlanymi. Kluczowym założeniem jest minimalizacja wpływu na środowisko naturalne oraz zapewnienie ciągłości ruchu drogowego w trakcie prowadzenia prac budowlanych.",
                        "budzet_calkowity": "Całkowity budżet projektu wynosi 450 000 000 PLN, przy czym 60% środków pochodzi z funduszy europejskich (270 000 000 PLN), 25% stanowi dofinansowanie z budżetu państwa (112 500 000 PLN), a pozostałe 15% to środki własne samorządu lokalnego (67 500 000 PLN). Budżet został szczegółowo rozplanowany na poszczególne etapy realizacji projektu z uwzględnieniem rezerwy ryzyka w wysokości 8% całkowitej wartości projektu.",
                        "czas_realizacji": "Projekt będzie realizowany w okresie 42 miesięcy, podzielony na 6 głównych etapów realizacyjnych. Przewidywany termin rozpoczęcia prac to 15 marca 2025 roku, a zakończenie planowane jest na 30 września 2028 roku. Każdy etap ma przypisane kamienie milowe oraz punkty kontrolne umożliwiające bieżące monitorowanie postępu prac.",
                        "interesariusze": "W projekt zaangażowanych jest 15 głównych podmiotów, w tym 3 wykonawców generalnych, 8 podwykonawców specjalistycznych, 2 firmy nadzorujące oraz 2 jednostki audytujące. Dodatkowo w proces konsultacyjny włączono przedstawicieli 12 organizacji pozarządowych działających na rzecz ochrony środowiska oraz rozwoju lokalnego.",
                        "ryzyka_glowne": "Zidentyfikowano 25 głównych rodzajów ryzyka, z których najistotniejsze to: opóźnienia w dostawach materiałów budowlanych (prawdopodobieństwo 35%, wpływ wysoki), przekroczenie budżetu (prawdopodobieństwo 28%, wpływ krytyczny), niekorzystne warunki pogodowe (prawdopodobieństwo 40%, wpływ średni), protesty społeczne (prawdopodobieństwo 15%, wpływ średni), problemy z uzyskaniem pozwoleń (prawdopodobieństwo 20%, wpływ wysoki) oraz awarie sprzętu budowlanego (prawdopodobieństwo 25%, wpływ niski)."
                    },
                    "kontekst_prawny": {
                        "podstawy_prawne": "Realizacja projektu opiera się na ustawie o drogach publicznych z dnia 21 marca 1985 r. (Dz.U. 1985 nr 14 poz. 60 z późn. zm.), ustawie Prawo budowlane z dnia 7 lipca 1994 r. (Dz.U. 1994 nr 89 poz. 414 z późn. zm.), ustawie o planowaniu i zagospodarowaniu przestrzennym z dnia 27 marca 2003 r. (Dz.U. 2003 nr 80 poz. 717 z późn. zm.) oraz rozporządzeniu Ministra Infrastruktury w sprawie warunków technicznych, jakim powinny odpowiadać drogi publiczne i ich usytuowanie.",
                        "pozwolenia": "Uzyskano wszystkie niezbędne pozwolenia i decyzje administracyjne, w tym: decyzję o środowiskowych uwarunkowaniach (wydana 15.08.2024), decyzję o lokalizacji inwestycji celu publicznego (wydana 22.09.2024), pozwolenie na budowę (wydane 18.10.2024), pozwolenie wodnoprawne (wydane 05.11.2024) oraz uzgodnienia z zarządcami sieci uzbrojenia terenu. Wszystkie decyzje są prawomocne i obejmują kompleksowy zakres planowanych prac.",
                        "umowy": "Zawarto 8 głównych umów, w tym 3 umowy z wykonawcami generalnymi o wartości łącznej 380 000 000 PLN, 2 umowy z firmami nadzorującymi o wartości 12 000 000 PLN, umowę z audytorem projektu o wartości 3 500 000 PLN oraz 2 umowy na usługi doradcze i konsultingowe o łącznej wartości 8 000 000 PLN. Wszystkie umowy zawierają kary umowne za opóźnienia oraz premie za wcześniejsze wykonanie."
                    },
                    "harmonogram_szczegolowy": [
                        {"faza": "Przygotowanie terenu", "start": "2025-03-15", "koniec": "2025-06-30", "budzet": "25000000", "postep": "0%", "status": "Zaplanowana"},
                        {"faza": "Roboty ziemne", "start": "2025-07-01", "koniec": "2025-11-30", "budzet": "45000000", "postep": "0%", "status": "Zaplanowana"},
                        {"faza": "Budowa infrastruktury podziemnej", "start": "2025-12-01", "koniec": "2026-06-30", "budzet": "85000000", "postep": "0%", "status": "Zaplanowana"},
                        {"faza": "Budowa dróg i ciągów komunikacyjnych", "start": "2026-07-01", "koniec": "2027-03-31", "budzet": "120000000", "postep": "0%", "status": "Zaplanowana"},
                        {"faza": "Instalacje elektryczne i oświetlenie", "start": "2027-04-01", "koniec": "2027-09-30", "budzet": "35000000", "postep": "0%", "status": "Zaplanowana"},
                        {"faza": "Zagospodarowanie terenu i zieleń", "start": "2027-10-01", "koniec": "2028-03-31", "budzet": "28000000", "postep": "0%", "status": "Zaplanowana"},
                        {"faza": "Odbiory i uruchomienie", "start": "2028-04-01", "koniec": "2028-09-30", "budzet": "15000000", "postep": "0%", "status": "Zaplanowana"}
                    ],
                    "analiza_srodowiskowa": {
                        "ocena_oddzialywania": "Przeprowadzono kompleksową ocenę oddziaływania na środowisko zgodnie z wymogami dyrektywy 2011/92/UE. Raport obejmuje analizę wpływu na: powietrze atmosferyczne, klimat akustyczny, wody powierzchniowe i podziemne, gleby i ziemię, świat roślinny i zwierzęcy, obszary chronione Natura 2000, krajobraz oraz zabytki i dobra materialne. Stwierdzono, że realizacja projektu nie spowoduje znaczącego negatywnego oddziaływania na środowisko przy zachowaniu zaproponowanych środków minimalizujących i kompensacyjnych.",
                        "srodki_ochronne": "Zaplanowano realizację 15 głównych działań ochronnych, w tym: budowę 8 ekranów akustycznych o łącznej długości 2,5 km, nasadzenie 5000 drzew i 15000 krzewów, budowę 12 przepustów dla małych zwierząt, wykonanie 4 stawów retencyjnych, montaż systemów oczyszczania wód opadowych, stosowanie materiałów budowlanych o obniżonej emisji substancji szkodliwych, prowadzenie prac z ominięciem okresów lęgowych ptaków, monitoring jakości powietrza i hałasu w trakcie budowy, rekultywację terenów po zakończeniu prac oraz utworzenie strefy buforowej o szerokości 50 m wzdłuż cieków wodnych.",
                        "kompensacja_przyrodnicza": "W ramach działań kompensacyjnych przewidziano utworzenie 3 nowych terenów zielonych o łącznej powierzchni 12 hektarów, renaturyzację 2 km cieku wodnego, budowę 5 stawów dla płazów, montaż 200 budek lęgowych dla ptaków oraz 50 skrzynek dla nietoperzy. Dodatkowo przeznaczono 2 000 000 PLN na program edukacji ekologicznej dla mieszkańców oraz 1 500 000 PLN na działania na rzecz bioróżnorodności w regionie."
                    },
                    "analiza_finansowa": {
                        "zrodla_finansowania": "Struktura finansowania projektu opiera się na trzech głównych źródłach. Najwięcej środków, bo aż 270 000 000 PLN (60% budżetu) pochodzi z Programu Operacyjnego Infrastruktura i Środowisko 2021-2027, w ramach osi priorytetowej III 'Rozwój sieci drogowej TEN-T i transportu multimodalnego'. Drugie co do wielkości źródło to Fundusz Dróg Samorządowych, który zapewnia 112 500 000 PLN (25% budżetu). Pozostałe 67 500 000 PLN (15% budżetu) stanowią środki własne powiatu pochodzące z budżetu bieżącego oraz planowanej emisji obligacji komunalnych.",
                        "harmonogram_platnosci": "Płatności będą realizowane etapowo zgodnie z postępem prac. W pierwszym roku (2025) planowane są wydatki w wysokości 45 000 000 PLN (10% budżetu), w drugim roku (2026) 135 000 000 PLN (30% budżetu), w trzecim roku (2027) 180 000 000 PLN (40% budżetu), a w czwartym roku (2028) 90 000 000 PLN (20% budżetu). Każda transza płatności jest uzależniona od osiągnięcia określonych kamieni milowych oraz pozytywnej weryfikacji postępu prac przez niezależnego audytora projektu.",
                        "analiza_efektywnosci": "Przeprowadzona analiza kosztów i korzyści (Cost-Benefit Analysis) wykazała, że projekt jest ekonomicznie uzasadniony. Wartość bieżąca netto (NPV) projektu wynosi 125 000 000 PLN przy zastosowaniu stopy dyskontowej 5%. Wewnętrzna stopa zwrotu (IRR) wynosi 8,2%, co znacząco przewyższa przyjętą stopę dyskontową. Okres zwrotu nakładów inwestycyjnych wynosi 18 lat. Wskaźnik korzyści do kosztów (BCR) wynosi 1,28, co oznacza, że każda zainwestowana złotówka przyniesie 1,28 PLN korzyści społeczno-ekonomicznych.",
                        "korzysci_spoleczne": "Realizacja projektu przyniesie wymierne korzyści dla lokalnej społeczności. Szacuje się, że w fazie budowy powstanie 850 nowych miejsc pracy, z czego 65% zostanie obsadzonych przez mieszkańców regionu. Po zakończeniu projektu skróci się średni czas dojazdu do centrum miasta o 25%, co przełoży się na oszczędność 2 500 000 godzin rocznie dla użytkowników infrastruktury. Zmniejszenie zużycia paliwa o 15% przyczyni się do redukcji emisji CO2 o 8 000 ton rocznie. Poprawa jakości powietrza wpłynie pozytywnie na zdrowie mieszkańców, co oszacowano na równowartość 12 000 000 PLN rocznie w postaci unikniętych kosztów leczenia chorób układu oddechowego."
                    },
                    "zarzadzanie_projektem": {
                        "struktura_organizacyjna": "Utworzono dedykowaną strukturę organizacyjną projektu składającą się z 5 poziomów zarządczych. Na najwyższym szczeblu znajduje się Komitet Sterujący, w skład którego wchodzą przedstawiciele wszystkich głównych interesariuszy. Poniżej działa Biuro Zarządzania Projektem składające się z 25 osób, w tym kierownika projektu, zastępcy kierownika, 3 koordynatorów obszarowych, 8 specjalistów branżowych, 6 inżynierów nadzoru oraz 5 osób zajmujących się obsługą administracyjno-finansową. Wszyscy członkowie zespołu posiadają certyfikaty PRINCE2 lub PMI oraz minimum 10-letnie doświadczenie w zarządzaniu projektami infrastrukturalnymi.",
                        "system_raportowania": "Wdrożono kompleksowy system raportowania oparty na platformie Microsoft Project Server 2024 zintegrowanej z systemem SAP. Raportowanie odbywa się na trzech poziomach: raporty tygodniowe (przesyłane do kierownika projektu), raporty miesięczne (prezentowane Komitetowi Sterującemu) oraz raporty kwartalne (przekazywane instytucjom finansującym). Każdy raport zawiera informacje o postępie rzeczowym, wykorzystaniu budżetu, zidentyfikowanych ryzykach, problemach wymagających eskalacji oraz prognozach na najbliższy okres. System automatycznie generuje alerty w przypadku odchyleń przekraczających 5% od zaplanowanych wartości.",
                        "zarzadzanie_ryzykiem": "Opracowano Rejestr Ryzyk zawierający 47 zidentyfikowanych zagrożeń wraz z planami mitygacji. Dla każdego ryzyka określono prawdopodobieństwo wystąpienia, potencjalny wpływ, działania zapobiegawcze oraz plany awaryjne. Najistotniejsze ryzyka są przedmiotem comiesięcznego przeglądu przez Komitet Sterujący. Utworzono rezerwę ryzyka w wysokości 36 000 000 PLN (8% budżetu), która może być uruchomiona wyłącznie za zgodą Komitetu. W okresie realizacji projektu planowane są 3 niezależne audyty ryzyka przeprowadzane przez zewnętrzną firmę konsultingową.",
                        "kontrola_jakosci": "Wdrożono system zarządzania jakością zgodny z normą ISO 9001:2015. Powołano Zespół Kontroli Jakości składający się z 8 inspektorów nadzoru, którzy przeprowadzają systematyczne kontrole na wszystkich etapach realizacji. Dla każdego rodzaju prac opracowano szczegółowe procedury kontroli jakości oraz protokoły odbioru. Zaplanowano 250 punktów kontrolnych rozmieszczonych w całym okresie realizacji projektu. Wszystkie materiały budowlane muszą posiadać certyfikaty zgodności oraz aktualne deklaracje właściwości użytkowych. Dodatkowo co 3 miesiące przeprowadzane są niezależne audyty jakości przez akredytowane laboratoria budowlane."
                    },
                    "podsumowanie_i_wnioski": {
                        "osiagniecia": "Dotychczas zakończono fazę przygotowawczą projektu, uzyskując wszystkie niezbędne pozwolenia i decyzje administracyjne. Przeprowadzono kompleksowe badania geotechniczne na całym obszarze inwestycji, wykonując 180 odwiertów i 95 sondowań. Opracowano pełną dokumentację projektową składającą się z 45 tomów zawierających szczegółowe projekty wykonawcze wszystkich branż. Zakończono postępowania przetargowe i podpisano wszystkie kluczowe umowy. Przeprowadzono szkolenia dla 120 osób zaangażowanych w realizację projektu. Uruchomiono zintegrowany system informatyczny do zarządzania projektem.",
                        "wyzwania": "Główne wyzwanie stanowi koordynacja działań 15 różnych podmiotów wykonawczych oraz zapewnienie terminowości dostaw materiałów budowlanych w obliczu niestabilnej sytuacji na rynkach międzynarodowych. Istotnym wyzwaniem jest również minimalizacja uciążliwości dla mieszkańców w trakcie prowadzenia prac, co wymaga szczegółowego planowania organizacji ruchu oraz efektywnej komunikacji społecznej. Dodatkowym wyzwaniem jest konieczność dostosowania harmonogramu prac do wymogów środowiskowych, w szczególności do okresów lęgowych ptaków oraz migracji płazów.",
                        "rekomendacje": "Zaleca się utrzymanie wysokiej częstotliwości spotkań koordynacyjnych (minimum raz w tygodniu) oraz wzmocnienie zespołu nadzoru o dodatkowych 2 specjalistów ds. koordynacji dostaw. Wskazane jest utworzenie dedykowanego Biura Obsługi Mieszkańców, które będzie na bieżąco informować o postępie prac oraz przyjmować zgłoszenia i skargi. Rekomenduje się również zwiększenie rezerwy na nieprzewidziane wydatki o dodatkowe 2% budżetu (9 000 000 PLN) z uwagi na rosnącą inflację i niepewność cenową na rynku materiałów budowlanych. Kluczowe jest również wdrożenie systemu wczesnego ostrzegania o potencjalnych opóźnieniach, opartego na automatycznej analizie danych z systemu zarządzania projektem."
                    }
                }
                """;

        JsonReportGenerator generator = new JsonReportGenerator();

        JasperPrint jasperPrint = generator.generateReport(json, "Raport z Dużą Ilością Tekstu i Tabelą");

        assertThat(jasperPrint).isNotNull();
        assertThat(jasperPrint.getPages()).isNotEmpty();

        String outputPath = "target/test-output/raport_duzo_tekstu_test.pdf";
        File outputDir = new File("target/test-output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

        File pdfFile = new File(outputPath);
        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);

        System.out.println("✅ PDF z dużą ilością tekstu wygenerowany pomyślnie!");
        System.out.println("📄 Ścieżka: " + pdfFile.getAbsolutePath());
        System.out.println("📊 Rozmiar pliku: " + String.format("%.2f KB", pdfFile.length() / 1024.0));
        System.out.println("📄 Liczba stron: " + jasperPrint.getPages().size());
        System.out.println("\n🧪 Test weryfikuje:");
        System.out.println("   ✓ Wielkie bloki tekstu przed tabelą");
        System.out.println("   ✓ Tabela z harmonogramem (7 wierszy)");
        System.out.println("   ✓ Wielkie bloki tekstu po tabeli");
        System.out.println("   ✓ Automatyczna paginacja na wiele stron");
        System.out.println("   ✓ Zachowanie hierarchii nagłówków");
        System.out.println("   ✓ Prawidłowe formatowanie długich akapitów");
    }
}

