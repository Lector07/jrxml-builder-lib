package pl.lib.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.lib.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RealizacjaRaport {

    public static class ReportData {
        private final JasperReport report;
        private final Map<String, Object> parameters;

        public ReportData(JasperReport report, Map<String, Object> parameters) {
            this.report = report;
            this.parameters = parameters;
        }

        public JasperReport getReport() { return report; }
        public Map<String, Object> getParameters() { return parameters; }
    }

    public static ReportData stworzRaport() throws JRException {
        ReportBuilder builder = new ReportBuilder()
                .withTitle("Raport realizacji")
                .withPageSize(842, 595)
                .withMargins(20, 20, 20, 20)
                .withCompanyInfo("Moja Firma", "ul. Przykładowa 1, 00-000 Miasto", "NIP: 123-456-78-90", "Tel: 123-456-789")
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
        builder.addColumn(new Column("departmentSymbol", "Dział", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE).withBox(true));
        builder.addColumn(new Column("value", "Wartość", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.NONE, Calculation.SUM, "KwotaStyle"));
        builder.addColumn(new Column("realizationAmount", "Kwota realizacji", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.NONE, Calculation.SUM, "KwotaStyle"));
        builder.addColumn(new Column("realizationBalanceAmount", "Pozostało", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM, "KwotaStyle"));


        // Dodanie grupowania po polu departmentSymbol (dział)
        builder.addGroup(new Group("internalNumber", "\"Nr wewnętrzny:  \" + $F{internalNumber}"));

        return new ReportData(builder.build(), builder.getParameters());
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
            ReportData reportData = stworzRaport();
            JasperReport raport = reportData.getReport();
            Map<String, Object> parametry = reportData.getParameters();

            List<Map<String, Object>> dane = wczytajDaneZPliku("realization.json");
            if (dane.isEmpty()) {
                System.err.println("Nie udało się wczytać danych z pliku. Raport nie zostanie wygenerowany.");
                return;
            }

            JRDataSource dataSource = new JRBeanCollectionDataSource(dane);

            parametry.put("ReportTitle", "Raport realizacji projektów");

            JasperPrint jasperPrint = JasperFillManager.fillReport(raport, parametry, dataSource);

            String sciezkaWyjsciowa = "raport_realizacji.pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, sciezkaWyjsciowa);

            System.out.println("Raport został pomyślnie wygenerowany i zapisany do pliku: " + sciezkaWyjsciowa);

        } catch (JRException e) {
            System.err.println("Błąd podczas generowania raportu PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}