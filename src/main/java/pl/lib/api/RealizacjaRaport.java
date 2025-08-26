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
import java.util.*;

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

    public static JasperReport stworzDesignPodraportu() throws JRException {
        ReportBuilder subreportBuilder = new ReportBuilder()
                .withMargins(0, 0, 0, 0)
;        ;

        Style headerStyle = new Style("SubreportHeader")
                .withFont("DejaVu Sans", 8, true)
                .withColors("#FFFFFF", "#7F7F7F")
                .withBorders(0.5f, "#000000")
                .withAlignment("Center", "Middle")
                .withPadding(2);

        Style dataStyle = new Style("SubreportData")
                .withFont("DejaVu Sans", 8, false)
                .withBorders(0.5f, "#000000")
                .withAlignment("Left", "Middle")
                .withPadding(2);

        Style kwotaStyle = new Style("SubreportKwota")
                .withFont("DejaVu Sans", 8, false)
                .withBorders(0.5f, "#000000")
                .withAlignment("Right", "Middle")
                .withPadding(2);

        subreportBuilder.addStyle(headerStyle).addStyle(dataStyle).addStyle(kwotaStyle);

        subreportBuilder.addColumn(new Column("typKosztu", "Typ Kosztu", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE, "SubreportData"));
        subreportBuilder.addColumn(new Column("kwota", "Kwota", 120, DataType.BIG_DECIMAL, "#,##0.00 PLN", Calculation.SUM, Calculation.SUM, "SubreportKwota"));


        return subreportBuilder.build();
    }

    public static ReportData stworzRaportGlowny(JasperReport podraportSkompilowany) throws JRException {
        ReportBuilder builder = new ReportBuilder()
                .withTitle("Raport realizacji")
                .withPageSize(842, 595)
                .withMargins(20, 20, 20, 20);

        String nazwaParametruDanych = "PODSUMOWANIE_KOSZTOW_DS";
        Subreport subreport = new Subreport("summary", podraportSkompilowany, nazwaParametruDanych);

        Style headerStyle = new Style("SubreportHeader")
                .withFont("DejaVu Sans", 8, true)
                .withColors("#FFFFFF", "#7F7F7F")
                .withBorders(0.5f, "#000000")
                .withAlignment("Center", "Middle")
                .withPadding(1);

        Style dataStyle = new Style("SubreportData")
                .withFont("DejaVu Sans", 8, false)
                .withBorders(0.5f, "#000000")
                .withAlignment("Left", "Middle")
                .withPadding(1);

        Style kwotaStyle = new Style("SubreportKwota")
                .withFont("DejaVu Sans", 8, false)
                .withBorders(0.5f, "#000000")
                .withAlignment("Right", "Middle")
                .withPadding(1);

        builder.addSubreport(subreport);

        builder.addStyle(dataStyle).addStyle(kwotaStyle).addStyle(headerStyle);


        builder.addColumn(new Column("id", "ID", -1, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE,"SubreportData").withBox(true));
        builder.addColumn(new Column("name", "Nazwa", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE,"SubreportData").withBox(true));
        builder.addColumn(new Column("departmentSymbol", "Dział", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE,"SubreportData").withBox(true));
        builder.addColumn(new Column("value", "Wartość", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM,"SubreportData").withBox(true));
        builder.addColumn(new Column("realizationAmount", "Kwota realizacji", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM,"SubreportData").withBox(true));
        builder.addColumn(new Column("realizationBalanceAmount", "Pozostało", -1, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.SUM, "SubreportData").withBox(true));

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
            List<Map<String, Object>> dane = mapper.readValue(plik, new TypeReference<List<Map<String, Object>>>() {});
            dane.forEach(wiersz -> {
                if (wiersz.containsKey("value")) wiersz.put("value", new BigDecimal(wiersz.get("value").toString()));
                if (wiersz.containsKey("realizationAmount")) wiersz.put("realizationAmount", new BigDecimal(wiersz.get("realizationAmount").toString()));
                if (wiersz.containsKey("realizationBalanceAmount")) wiersz.put("realizationBalanceAmount", new BigDecimal(wiersz.get("realizationBalanceAmount").toString()));
            });
            return dane;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        try {
            JasperReport podraportDesign = stworzDesignPodraportu();
            ReportData reportData = stworzRaportGlowny(podraportDesign);
            JasperReport raportGlowny = reportData.getReport();

            List<Map<String, Object>> danePodraportu = new ArrayList<>();
            danePodraportu.add(new HashMap<String, Object>() {{ put("typKosztu", "Koszty materiałów"); put("kwota", new BigDecimal("12500.75")); }});
            danePodraportu.add(new HashMap<String, Object>() {{ put("typKosztu", "Wynagrodzenia"); put("kwota", new BigDecimal("85300.00")); }});
            JRDataSource dataSourcePodraportu = new JRBeanCollectionDataSource(danePodraportu);

            List<Map<String, Object>> daneGlowne = wczytajDaneZPliku("realization.json");
            if (daneGlowne.isEmpty()) {
                System.err.println("Nie udało się wczytać danych z pliku. Raport nie zostanie wygenerowany.");
                return;
            }
            JRDataSource dataSourceGlowny = new JRBeanCollectionDataSource(daneGlowne);

            Map<String, Object> parametry = reportData.getParameters();
            parametry.put("ReportTitle", "Raport realizacji projektów");
            parametry.put("SUBREPORT_OBJECT_PODSUMOWANIE_KOSZTOW_DS", podraportDesign);
            parametry.put("PODSUMOWANIE_KOSZTOW_DS", dataSourcePodraportu);

            JasperPrint jasperPrint = JasperFillManager.fillReport(raportGlowny, parametry, dataSourceGlowny);

            String sciezkaWyjsciowa = "raport_z_podraportem.pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, sciezkaWyjsciowa);

            System.out.println("Raport został pomyślnie wygenerowany i zapisany do pliku: " + sciezkaWyjsciowa);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }
}