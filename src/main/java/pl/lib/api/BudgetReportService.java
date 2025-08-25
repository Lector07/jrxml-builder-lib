package pl.lib.api;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import pl.lib.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BudgetReportService {

    public byte[] generateExpensesReport(String jsonData) throws JRException {

        ReportBuilder builder = new ReportBuilder();

        Style headerStyle = new Style("HeaderWithBorder")
                .withFont("DejaVu Sans Condensed", 12, true)
                .withAlignment("Center", "Middle")
                .withBorders(1.0f, "#000000");
        builder.addStyle(headerStyle)
                .withHeaderStyle("HeaderWithBorder");

        Group expensesGroup = new Group("kategoria", "\"Kategoria: \" + $F{kategoria}");

        JasperReport jasperReport = builder
                .withTitle("Zestawienie Wydatków")
                .withPageSize(842, 595)
                .withHeaderStyle("HeaderStyle")
                .withStandardFooter("eBudżet - ZSI \"Sprawny Urząd\"\n" +
                        "BUK Softres - www.softres.pl", null)
                .addGroup(expensesGroup)
                .addStyle(new Style("BoxedStyle").withBorders(1.0f, "#000000"))
                .addColumn(new Column("kategoria", "Kategoria", 0, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true))
                .addColumn(new Column("data", "Data", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true))
                .addColumn(new Column("opis", "Opis", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true))
                .addColumn(new Column("kwota", "Kwota", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM).withBox(true))
                .build();

        InputStream jsonStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
        JRDataSource jsonDataSource = new JsonDataSource(jsonStream, "wydatki");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Raport Wydatków - Sierpień 2025");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jsonDataSource);


        System.out.println(builder.getJrxmlContent());
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generateIncomeReport(String jsonData) throws JRException {
        ReportBuilder builder = new ReportBuilder();

        Style groupHeaderStyle = new Style("GroupHeaderStyle")
                .withFont("DejaVu Sans", 10, true)
                .withColors("#005500", null);

        Style positiveAmountStyle = new Style("PositiveAmount")
                .withFont("DejaVu Sans", 10, true)
                .withColors("#006600", null);

        builder.addStyle(groupHeaderStyle);
        builder.addStyle(positiveAmountStyle);

        Group incomeGroup = new Group("zrodlo", "\"Źródło: \" + $F{zrodlo}");

        JasperReport jasperReport = builder
                .withTitle("Zestawienie Przychodów")
                .withPageSize(842, 595)
                .withStandardFooter("eBudżet - ZSI \"Sprawny Urząd\"\n" +
                        "BUK Softres - www.softres.pl", null)
                .addGroup(incomeGroup)
                .addStyle(new Style("BoxedStyle").withBorders(1.0f, "#000000"))

                .addColumn(new Column("zrodlo", "Źródło", 0, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("data", "Data", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("opis", "Opis", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("kwota", "Kwota", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM, "PositiveAmount"))
                .build();

        InputStream jsonStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
        JRDataSource jsonDataSource = new JsonDataSource(jsonStream, "przychody");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Raport Przychodów - Sierpień 2025");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jsonDataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generateBudgetSummaryReport(String jsonData) throws JRException {
        ReportBuilder builder = new ReportBuilder();

        Style positiveStyle = new Style("PositiveAmount")
                .withFont("DejaVu Sans", 10, true)
                .withColors("#006600", null);

        Style negativeStyle = new Style("NegativeAmount")
                .withFont("DejaVu Sans", 10, true)
                .withColors("#CC0000", null);

        Style headerStyle = new Style("HeaderStyle")
                .withFont("DejaVu Sans", 11, true)
                .withColors("#000000", "#E6E6E6");

        builder.addStyle(positiveStyle);
        builder.addStyle(negativeStyle);
        builder.addStyle(headerStyle);

        JasperReport jasperReport = builder
                .withTitle("Podsumowanie Budżetu")
                .withPageSize(595, 842) // A4 Pionowo
                .withStandardFooter("eBudżet - ZSI \"Sprawny Urząd\"\n" +
                        "BUK Softres - www.softres.pl", null)
                .addColumn(new Column("typ", "Typ", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true))
                .addColumn(new Column("kategoria", "Kategoria/Źródło", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true))
                .addColumn(new Column("kwota", "Kwota", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.NONE, Calculation.SUM).withBox(true))
                .build();

        InputStream jsonStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
        JRDataSource jsonDataSource = new JsonDataSource(jsonStream, "zestawienie");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Podsumowanie Budżetu - Sierpień 2025");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jsonDataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generateMonthlyReport(String jsonData, String month, String year) throws JRException {
        ReportBuilder builder = new ReportBuilder();

        Style totalStyle = new Style("TotalStyle")
                .withFont("DejaVu Sans", 11, true)
                .withColors("#000000", "#F0F0F0")
                .withBorders(1.0f, "#000000");

        builder.addStyle(totalStyle);

        Group dateGroup = new Group("tydzien", "\"Tydzień: \" + $F{tydzien}");

        JasperReport jasperReport = builder
                .withTitle("Raport Miesięczny: " + month + " " + year)
                .withPageSize(842, 595) // A4 Poziomo
                .withStandardFooter("eBudżet - ZSI \"Sprawny Urząd\"\n" +
                        "BUK Softres - www.softres.pl", null)
                .addGroup(dateGroup)
                .addStyle(new Style("BoxedStyle").withBorders(1.0f, "#000000"))

                .addColumn(new Column("tydzien", "Tydzień", 0, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("data", "Data", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("kategoria", "Kategoria", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("opis", "Opis", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("przychod", "Przychód", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM))
                .addColumn(new Column("wydatek", "Wydatek", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM))
                .addColumn(new Column("bilans", "Bilans", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM))
                .build();

        InputStream jsonStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
        JRDataSource jsonDataSource = new JsonDataSource(jsonStream, "transakcje");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Raport Miesięczny: " + month + " " + year);

        System.out.println();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jsonDataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public static void main(String[] args) {
        String jsonData = "{\n" +
                "  \"wydatki\": [\n" +
                "    {\n" +
                "      \"kategoria\": \"Jedzenie\",\n" +
                "      \"opis\": \"Zakupy w supermarkecie\",\n" +
                "      \"kwota\": 250.75,\n" +
                "      \"data\": \"2025-08-05\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"kategoria\": \"Transport\",\n" +
                "      \"opis\": \"Bilet miesięczny\",\n" +
                "      \"kwota\": 120.00,\n" +
                "      \"data\": \"2025-08-01\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"kategoria\": \"Jedzenie\",\n" +
                "      \"opis\": \"Pizza\",\n" +
                "      \"kwota\": 45.50,\n" +
                "      \"data\": \"2025-08-08\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"kategoria\": \"Rozrywka\",\n" +
                "      \"opis\": \"Bilety do kina\",\n" +
                "      \"kwota\": 60.00,\n" +
                "      \"data\": \"2025-08-10\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        BudgetReportService reportService = new BudgetReportService();
        try {
            byte[] pdfBytes = reportService.generateExpensesReport(jsonData);
            byte[] pdfBytesIncome = reportService.generateIncomeReport(jsonData);
            byte[] pdfBytesSummary = reportService.generateBudgetSummaryReport(jsonData);
            byte[] pdfBytesMonthly = reportService.generateMonthlyReport(jsonData, "Sierpień", "2025");

            java.nio.file.Files.write(java.nio.file.Paths.get("raport_wydatkow.pdf"), pdfBytes);
            java.nio.file.Files.write(java.nio.file.Paths.get("raport_przychodow.pdf"), pdfBytesIncome);
            java.nio.file.Files.write(java.nio.file.Paths.get("podsumowanie_budzetu.pdf"), pdfBytesSummary);
            java.nio.file.Files.write(java.nio.file.Paths.get("raport_miesieczny.pdf"), pdfBytesMonthly);


            System.out.println("Raport PDF został pomyślnie wygenerowany: raport_wydatkow.pdf");

        } catch (JRException | java.io.IOException e) {
            e.printStackTrace();
        }
    }
}

    //TODO : Proponowany Plan Rozwoju (Roadmap)
    //Krok 1 (Fundament): Skoncentruj 100% wysiłków na refaktoryzacji ReportBuilder z StringBuilder na JasperDesign. To najważniejszy dług technologiczny do spłacenia. Wszystkie obecne testy powinny nadal przechodzić po tej zmianie.
    //Krok 2 (Ulepszenia API):
    //Zaimplementuj obsługę wielu grup (List<Group>).
    //Stwórz bardziej elastyczny system stylów (np. prostą klasę Theme).
    //Krok 3 (Nowe Funkcje):
    //Dodaj wsparcie dla innych podstawowych elementów: linii (JRDesignLine), prostokątów (JRDesignRectangle), pól statycznych (JRDesignStaticText) w różnych sekcjach.
    //Wprowadź obsługę podraportów (Subreport).
    //Krok 4 (Dojrzałość):
    //Uzupełnij Javadoc.
    //Wprowadź walidację danych wejściowych.
    //Dodaj więcej przykładów użycia (np. w postaci testów lub klasy Examples).
    //Podsumowanie
    //Stworzyłeś świetny prototyp z bardzo dobrze zaprojektowanym, zorientowanym na użytkownika API. To najtrudniejsza część i masz ją za sobą. Teraz nadszedł czas, aby wymienić "silnik" (generowanie XML) na bardziej profesjonalne, obiektowe rozwiązanie, które oferuje samo JasperReports. Ta jedna zmiana otworzy Ci drzwi do nieograniczonych możliwości, zwiększy stabilność i uczyni dalszy rozwój biblioteki znacznie prostszym.
    //Gratuluję dotychczasowej pracy i powodzenia w dalszym rozwoju projektu

    //TODO: Jasne, oto szczegółowy plan wdrożenia funkcji automatycznego generowania raportu w formie listy TODO.
    //
    //Plan Wdrożenia: Inteligentny Generator Raportów
    //Etap 1: Przygotowanie Projektu
    //
    //Zadanie 1: Dodaj zależność do biblioteki Jackson
    //
    //Cel: Umożliwienie parsowania danych w formacie JSON.
    //
    //Akcja: W zależności od Twojego narzędzia do budowania, dodaj odpowiedni wpis.
    //
    //Maven (pom.xml):
    //
    //code
    //Xml
    //download
    //content_copy
    //expand_less
    //
    //<dependency>
    //    <groupId>com.fasterxml.jackson.core</groupId>
    //    <artifactId>jackson-databind</artifactId>
    //    <version>2.15.2</version> <!-- Sprawdź najnowszą wersję -->
    //</dependency>
    //
    //Gradle (build.gradle):
    //
    //code
    //Groovy
    //download
    //content_copy
    //expand_less
    //IGNORE_WHEN_COPYING_START
    //IGNORE_WHEN_COPYING_END
    //implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    //Etap 2: Stworzenie Logiki Wnioskującej
    //
    //Zadanie 2: Stwórz nową klasę AutoReportGenerator
    //
    //Cel: Odizolowanie logiki automatycznego wnioskowania od ReportBuilder.
    //
    //Akcja: W pakiecie pl.lib.api utwórz nową klasę public class AutoReportGenerator.
    //
    //Szczegóły:
    //
    //Dodaj prywatne pole: private final ReportBuilder reportBuilder;
    //
    //Stwórz konstruktor, który inicjalizuje to pole: public AutoReportGenerator() { this.reportBuilder = new ReportBuilder(); }
    //
    //Zadanie 3: Zaimplementuj główną metodę generate
    //
    //Cel: Stworzenie publicznego API dla nowej funkcjonalności.
    //
    //Akcja: Dodaj w AutoReportGenerator metodę:
    //
    //code
    //Java
    //download
    //content_copy
    //expand_less
    //IGNORE_WHEN_COPYING_START
    //IGNORE_WHEN_COPYING_END
    //public JasperReport generate(String jsonData, String dataPath) throws IOException, JRException
    //
    //Logika wewnątrz metody:
    //
    //Użyj ObjectMapper z Jackson, aby sparsować jsonData do List<Map<String, Object>>.
    //
    //Sprawdź, czy lista danych nie jest pusta. Jeśli tak, rzuć IllegalArgumentException.
    //
    //Wywołaj (jeszcze nieistniejące) metody pomocnicze: List<Column> inferredColumns = inferColumns(data); oraz String groupingFieldName = inferGrouping(data, inferredColumns);
    //
    //Użyj reportBuilder do skonfigurowania raportu na podstawie wyników z punktu 3.
    //
    //Zbuduj i zwróć raport: return reportBuilder.build();
    //
    //Zadanie 4: Zaimplementuj metodę do wnioskowania o kolumnach inferColumns
    //
    //Cel: Automatyczne wykrywanie kolumn i ich typów.
    //
    //Akcja: Stwórz w AutoReportGenerator prywatną metodę: private List<Column> inferColumns(List<Map<String, Object>> data)
    //
    //Logika wewnątrz metody:
    //
    //Weź pierwszy rekord z listy (data.get(0)).
    //
    //Przeiteruj po jego kluczach (entrySet()).
    //
    //Dla każdego klucza:
    //
    //Utwórz nowy obiekt Column.
    //
    //Nazwa pola (fieldName) to klucz.
    //
    //Tytuł (title) to klucz zamieniony na wielkie litery.
    //
    //Typ (DataType) ustal na podstawie klasy wartości (value.getClass()), używając do tego metody pomocniczej mapJavaTypeToDataType.
    //
    //Ustaw domyślne obliczenia: jeśli typ jest numeryczny, ustaw Calculation.SUM dla podsumowań grupy i raportu. W przeciwnym razie Calculation.NONE.
    //
    //Zwróć listę utworzonych kolumn.
    //
    //Zadanie 5: Zaimplementuj metodę do wnioskowania o grupowaniu inferGrouping
    //
    //Cel: Znalezienie najlepszego kandydata na pole do grupowania.
    //
    //Akcja: Stwórz w AutoReportGenerator prywatną metodę: private String inferGrouping(List<Map<String, Object>> data, List<Column> columns)
    //
    //Logika wewnątrz metody (heurystyka):
    //
    //Filtruj listę kolumn, aby znaleźć tylko te typu STRING.
    //
    //Dla każdej kolumny tekstowej oblicz jej kardynalność (liczbę unikalnych wartości).
    //
    //Wybierz pole, które spełnia warunki:
    //
    //Liczba unikalnych wartości > 1 (grupowanie po polu z jedną wartością nie ma sensu).
    //
    //Liczba unikalnych wartości jest relatywnie mała (np. < data.size() / 2).
    //
    //Spośród kandydatów wybierz tego z najmniejszą liczbą unikalnych wartości.
    //
    //Zwróć jego nazwę (fieldName) lub null, jeśli nie znaleziono dobrego kandydata.
    //
    //Zadanie 6: Stwórz pomocniczą metodę mapJavaTypeToDataType
    //
    //Cel: Mapowanie klas Javy na Twój enum DataType.
    //
    //Akcja: Stwórz w AutoReportGenerator prywatną metodę: private DataType mapJavaTypeToDataType(Class<?> javaClass)
    //
    //Logika wewnątrz metody: Użyj serii instrukcji if lub switch, aby zwrócić odpowiednią wartość DataType (np. if (Integer.class.isAssignableFrom(javaClass)) return DataType.INTEGER;). Domyślnie zwracaj DataType.STRING.
    //
    //Etap 3: Integracja i Testowanie
    //
    //Zadanie 7: Zaktualizuj BudgetReportService
    //
    //Cel: Wykorzystanie nowo stworzonego AutoReportGenerator.
    //
    //Akcja: W metodzie generateExpensesReport zamień istniejącą logikę budowania raportu na:
    //
    //code
    //Java
    //download
    //content_copy
    //expand_less
    //IGNORE_WHEN_COPYING_START
    //IGNORE_WHEN_COPYING_END
    //// Stara logika do usunięcia lub zakomentowania
    //// ReportBuilder builder = new ReportBuilder();
    //// builder.withTitle(...).addColumn(...) ...
    //
    //// Nowa logika
    //AutoReportGenerator autoGenerator = new AutoReportGenerator();
    //JasperReport jasperReport = autoGenerator.generate(jsonData, "wydatki");
    //
    //Zadanie 8: Stwórz testy jednostkowe dla AutoReportGenerator
    //
    //Cel: Weryfikacja poprawności działania nowej logiki.
    //
    //Akcja: Utwórz klasę AutoReportGeneratorTest.
    //
    //Sugerowane przypadki testowe:
    //
    //Test Głównej Funkcjonalności: Użyj przykładowego JSON-a z wydatkami i sprawdź, czy:
    //
    //Poprawnie zidentyfikowano wszystkie kolumny.
    //
    //Pole "kategoria" zostało wybrane do grupowania.
    //
    //Dla pola "kwota" ustawiono obliczenia SUM.
    //
    //Test Braku Grupowania: Użyj danych, gdzie każde pole tekstowe ma unikalne wartości. Sprawdź, czy metoda inferGrouping zwróciła null i raport został wygenerowany bez grupowania.
    //
    //Test Pustych Danych: Przekaż pusty JSON i upewnij się, że rzucany jest oczekiwany wyjątek.
    //
    //Etap 4: Zadania Dodatkowe (Po Wdrożeniu Podstawowej Wersji)
    //
    //Zadanie 9: Popraw generowanie tytułów kolumn
    //
    //Cel: Zamiast NAZWA_POLA generować Nazwa Pola.
    //
    //Akcja: W inferColumns dodaj prostą logikę do formatowania tytułu.
    //
    //Zadanie 10: Dodaj wykrywanie formatu daty
    //
    //Cel: Rozpoznawanie stringów wyglądających jak daty i mapowanie ich na DataType.DATE.
    //
    //Akcja: W mapJavaTypeToDataType, jeśli typem jest String, spróbuj sparsować wartość, aby sprawdzić, czy pasuje do popularnych formatów dat.