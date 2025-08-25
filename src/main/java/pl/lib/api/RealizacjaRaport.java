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
                .withCompanyInfo("BIURO USŁUG KOMPUTEROWYCH \"SOFTRES\" SP Z O.O", "ul. Zaciszna 44, 35-326 Rzeszów", "NIP: 8130335217", "Regon: 690037603")
                .withStandardFooter("Wygenerowano: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), "Dokument wewnętrzny");

        Style headerStyle = new Style("TableHeaderStyle")
                .withFont("DejaVu Sans", 10, false)
                .withColors("#FFFFFF", "#2A3F54")
                .withAlignment("Center", "Middle")
                .withBorders(0.2f, "#000000").withPadding(6);

        Style dataStyle = new Style("TableDataStyle") // Styl dla danych
                .withFont("DejaVu Sans", 8, false)
                .withAlignment("Left", "Middle")
                .withBorders(0.2f, "#000000").withPadding(6);

        Style kwotaStyle = new Style("KwotaStyle")
                .withFont("DejaVu Sans", 8, false)
                .withAlignment("Right", "Middle")
                .withBorders(0.2f, "#000000").withPadding(6);

        Style groupHeaderStyle = new Style("GroupHeaderStyle")
                .withFont("DejaVu Sans", 10, true)
                .withColors("#000000", "#E6E6E6")
                .withAlignment("Left", "Middle")
                .withBorders(0.2f, "#000000").withPadding(6);


        builder.addStyle(headerStyle);
        builder.addStyle(dataStyle);
        builder.addStyle(kwotaStyle);
        builder.addStyle(groupHeaderStyle);

        builder.addColumn(new Column("id", "ID", 80, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE, "dataStyle").withBox(true));
        builder.addColumn(new Column("name", "Nazwa", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE, "dataStyle").withBox(true));
        builder.addColumn(new Column("departmentSymbol", "Dział", 100, DataType.STRING, null, Calculation.NONE, Calculation.NONE, "dataStyle").withBox(true));
        builder.addColumn(new Column("value", "Wartość", 120, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM, "kwotaStyle").withBox(true));
        builder.addColumn(new Column("realizationAmount", "Kwota realizacji", 120, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM, "kwotaStyle").withBox(true));
        builder.addColumn(new Column("realizationBalanceAmount", "Pozostało", 120, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM, "kwotaStyle").withBox(true));

        Group subGroup = new Group("internalNumber", "\"Nr wewnętrzny: \" + $F{internalNumber}", "kwotaStyle", false).withHeaderStyle("GroupHeaderStyle").withShowGroupFooter(true);
        Group group = new Group("edType", "\"Typ: \" + $F{edType}", "kwotaStyle", false).withHeaderStyle("GroupHeaderStyle").withShowGroupFooter(true);
        builder.addGroup(group);
        builder.addGroup(subGroup);

        System.out.println(builder.getJrxmlContent());


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
                if (wiersz.containsKey("value")) wiersz.put("value", new BigDecimal(wiersz.get("value").toString()));
                if (wiersz.containsKey("realizationAmount")) wiersz.put("realizationAmount", new BigDecimal(wiersz.get("realizationAmount").toString()));
                if (wiersz.containsKey("realizationBalanceAmount")) wiersz.put("realizationBalanceAmount", new BigDecimal(wiersz.get("realizationBalanceAmount").toString()));
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