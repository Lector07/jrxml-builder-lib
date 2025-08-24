package pl.lib.api;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import pl.lib.api.ReportBuilder; // Twoja klasa ReportBuilder
import pl.lib.model.Calculation;
import pl.lib.model.Column;
import pl.lib.model.DataType;
import pl.lib.model.Group;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BudgetReportService {

    /**
     * Generuje raport wydatków w formacie PDF na podstawie danych JSON.
     *
     * @param jsonData String zawierający dane w formacie JSON.
     * @return Tablica bajtów (byte[]) reprezentująca plik PDF.
     * @throws JRException jeśli wystąpi błąd podczas generowania raportu.
     */
    public byte[] generateExpensesReport(String jsonData) throws JRException {

        ReportBuilder builder = new ReportBuilder();
        String jrxmlTemplate = builder
                .withTitle("Zestawienie Wydatków")
                .withPageSize(595, 842) // A4
                .withStandardFooter("Wygenerowano przez e-budżet", "Poufne")
                .addGroup(new Group("kategoria", "\"Kategoria: \" + $F{kategoria}")) // Grupowanie po kategorii
                .addColumn(new Column("data", "Data", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("opis", "Opis", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE)) // -1 = auto szerokość
                .addColumn(new Column("kwota", "Kwota", -1, DataType.BIG_DECIMAL, "#,##0.00 PLN", Calculation.SUM, Calculation.SUM))
                .build();

        System.out.println("--- Wygenerowany JRXML ---");
        System.out.println(jrxmlTemplate);
        System.out.println("--------------------------");

        InputStream jrxmlStream = new ByteArrayInputStream(jrxmlTemplate.getBytes(StandardCharsets.UTF_8));
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        InputStream jsonStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
        JRDataSource jsonDataSource = new JsonDataSource(jsonStream, "wydatki");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Raport Wydatków - Sierpień 2025");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jsonDataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    // Metoda main do przetestowania serwisu
    public static void main(String[] args) {
        // Przykładowe dane JSON
        String jsonData = "{\n" +
                "  \"budzetInfo\": {\n" +
                "    \"nazwa\": \"Budżet domowy - Sierpień 2025\",\n" +
                "    \"wlasciciel\": \"Jan Kowalski\"\n" +
                "  },\n" +
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

            // Zapisz plik PDF na dysku w celu weryfikacji
            java.nio.file.Files.write(java.nio.file.Paths.get("raport_wydatkow.pdf"), pdfBytes);
            System.out.println("Raport PDF został pomyślnie wygenerowany: raport_wydatkow.pdf");

        } catch (JRException | java.io.IOException e) {
            e.printStackTrace();
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
}