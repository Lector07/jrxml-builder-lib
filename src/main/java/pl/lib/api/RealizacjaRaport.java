package pl.lib.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.lib.model.Column;
import pl.lib.model.DataType;
import pl.lib.model.Calculation;
import pl.lib.model.Style;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.text.SimpleDateFormat;

public class RealizacjaRaport {

    public static JasperReport stworzRaport() throws JRException {
        ReportBuilder builder = new ReportBuilder()
                .withTitle("Raport realizacji")
                .withPageSize(842, 595)
                .withMargins(20, 20, 20, 20)
                .withStandardFooter("Wygenerowano: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), "Dokument wewnętrzny");

        Style headerStyle = new Style("NaglowekStyle")
                .withFont("DejaVu Sans", 10, true)
                .withColors("#FFFFFF", "#2A3F54")
                .withAlignment("Center", "Middle");

        Style kwotaStyle = new Style("KwotaStyle")
                .withFont("DejaVu Sans", 9, false)
                .withAlignment("Right", "Middle")
                .withBorders(0.5f, "#000000");

        builder.addStyle(headerStyle);
        builder.addStyle(kwotaStyle);

        builder.addColumn(new Column("id", "ID", -1, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE).withBox(true));
        builder.addColumn(new Column("name", "Nazwa", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true));
        builder.addColumn(new Column("internalNumber", "Nr wew.", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true));
        builder.addColumn(new Column("departmentSymbol", "Dział", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true));
        builder.addColumn(new Column("value", "Wartość", -1, DataType.BIG_DECIMAL, "#,##0.00 zł", Calculation.NONE, Calculation.SUM, "KwotaStyle"));
        builder.addColumn(new Column("realizationAmount", "Kwota realizacji", -1, DataType.BIG_DECIMAL, "#,##0.00 zł", Calculation.NONE, Calculation.SUM, "KwotaStyle"));
        builder.addColumn(new Column("realizationBalanceAmount", "Pozostało", -1, DataType.BIG_DECIMAL, "#,##0.00 zł", Calculation.NONE, Calculation.SUM, "KwotaStyle"));
        builder.withStandardFooter("Wygenerowano: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), "Dokument wewnętrzny");

        return builder.build();
    }

    private static List<Map<String, Object>> wczytajDaneZPliku(String sciezkaPliku) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File plik = new File(sciezkaPliku);
            if (!plik.exists()) {
                System.err.println("Plik " + sciezkaPliku + " nie istnieje.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> dane = mapper.readValue(plik,
                    new TypeReference<List<Map<String, Object>>>() {});

            // Konwersja wartości liczbowych na BigDecimal
            dane.forEach(wiersz -> {
                if (wiersz.containsKey("value")) {
                    wiersz.put("value", new BigDecimal(wiersz.get("value").toString()));
                }
                if (wiersz.containsKey("realizationAmount")) {
                    wiersz.put("realizationAmount", new BigDecimal(wiersz.get("realizationAmount").toString()));
                }
                if (wiersz.containsKey("realizationBalanceAmount")) {
                    wiersz.put("realizationBalanceAmount", new BigDecimal(wiersz.get("realizationBalanceAmount").toString()));
                }
            });

            return dane;
        } catch (IOException e) {
            System.err.println("Błąd podczas wczytywania danych z pliku JSON: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        try {
            // Generowanie raportu
            JasperReport raport = stworzRaport();

            // Wczytanie danych z pliku JSON
            List<Map<String, Object>> dane = wczytajDaneZPliku("realization.json");
            if (dane.isEmpty()) {
                System.err.println("Nie udało się wczytać danych z pliku. Raport nie zostanie wygenerowany.");
                return;
            }

            JRDataSource dataSource = new JRBeanCollectionDataSource(dane);

            // Parametry raportu
            Map<String, Object> parametry = new HashMap<>();
            parametry.put("ReportTitle", "Raport realizacji projektów");

            // Wypełnienie raportu danymi
            JasperPrint jasperPrint = JasperFillManager.fillReport(raport, parametry, dataSource);

            // Określenie ścieżki wyjściowej dla pliku PDF
            String sciezkaWyjsciowa = "raport_realizacji.pdf";

            // Eksport do PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint, sciezkaWyjsciowa);

            System.out.println("Raport został pomyślnie wygenerowany i zapisany do pliku: " + sciezkaWyjsciowa);

        } catch (JRException e) {
            System.err.println("Błąd podczas generowania raportu PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}