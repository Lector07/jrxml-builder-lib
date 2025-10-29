package pl.lib.api;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.*;
import org.junit.jupiter.api.Test;
import pl.lib.automation.JsonReportGenerator;
import pl.lib.config.ColumnDefinition;
import pl.lib.config.GroupDefinition;
import pl.lib.config.ReportConfig;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonReportGeneratorTest {

    @Test
    void generateReport_simpleObject_returnsPrintAndSetsDesign() throws Exception {
        // given
        String json = "{ \"name\": \"Alice\", \"age\": 30 }";
        JsonReportGenerator gen = new JsonReportGenerator();

        // when
        JasperPrint print = gen.generateReport(json, "Test");

        // then
        assertNotNull(print, "Powinien zostać zwrócony JasperPrint.");
        assertNotNull(gen.getLastGeneratedDesign(), "Powinien zostać ustawiony ostatni JasperDesign.");
        // weryfikacja, że sekcja summary istnieje i zawiera elementy (pola Key-Value)
        JRDesignBand summary = (JRDesignBand) gen.getLastGeneratedDesign().getSummary();
        assertNotNull(summary, "Summary band powinien istnieć.");
        assertTrue(summary.getElements() != null && summary.getElements().length > 0, "Summary band powinien zawierać elementy.");
    }

    @Test
    void generateReport_arrayOfObjects_addsSubreportElement() throws Exception {
        // given
        String json = "{ \"items\": [ {\"a\":1, \"b\":\"x\"}, {\"a\":2, \"b\":\"y\"} ] }";
        JsonReportGenerator gen = new JsonReportGenerator();

        // when
        gen.generateReport(json, "ArrayReport");

        // then
        JRDesignBand summary = (JRDesignBand) gen.getLastGeneratedDesign().getSummary();
        assertNotNull(summary, "Summary band powinien istnieć.");
        boolean hasSubreport = Arrays.stream(summary.getElements())
                .anyMatch(e -> e instanceof JRDesignSubreport);
        assertTrue(hasSubreport, "Powinien zostać dodany element subraportu dla tablicy obiektów.");
    }

    @Test
    void generateReport_nestedObject_addsIndentedHeader() throws Exception {
        String json = "{ \"person\": { \"name\": \"John\" } }";
        JsonReportGenerator gen = new JsonReportGenerator();

        gen.generateReport(json, "NestedReport");

        JRDesignBand summary = (JRDesignBand) gen.getLastGeneratedDesign().getSummary();
        assertNotNull(summary, "Summary band powinien istnieć.");

        Optional<JRDesignTextField> personHeader = Arrays.stream(summary.getElements())
                .filter(e -> e instanceof JRDesignTextField)
                .map(e -> (JRDesignTextField) e)
                .filter(st -> {
                    JRDesignExpression expr = (JRDesignExpression) st.getExpression();
                    return expr != null && "\"person\"".equals(expr.getText());
                })
                .findFirst();

        assertTrue(personHeader.isPresent(), "Powinien istnieć nagłówek dla klucza 'person'.");
        assertEquals(15, personHeader.get().getX(), "Nagłówek z poziomu 1 powinien mieć wcięcie X=15.");
    }


    @Test
    void generateTableReportFromJson_throwsWhenNotArray() {
        String notArrayJson = "{ \"k\": 1 }";
        JsonReportGenerator gen = new JsonReportGenerator();

        assertThrows(IllegalArgumentException.class, () ->
                gen.generateTableReportFromJson(notArrayJson, new MinimalReportConfig("T")), "Dla nie-tablicy powinien zostać rzucony IllegalArgumentException.");
    }

    @Test
    void generateTableReportFromJson_minimalConfig_succeeds() throws Exception {
        String arrayJson = "[ {\"id\":1, \"name\":\"A\"}, {\"id\":2, \"name\":\"B\"} ]";
        JsonReportGenerator gen = new JsonReportGenerator();
        ReportConfig cfg = new MinimalReportConfig("TableTitle");

        JasperPrint print = gen.generateTableReportFromJson(arrayJson, cfg);

        assertNotNull(print, "Powinien zostać zwrócony JasperPrint dla tablicy JSON.");
        assertNotNull(gen.getLastGeneratedDesign(), "Powinien zostać ustawiony ostatni JasperDesign.");
    }


    private static class MinimalReportConfig extends ReportConfig {
        private final String title;

        MinimalReportConfig(String title) {
            this.title = title;
        }

        @Override public String getTitle() { return title; }
        @Override public String getOrientation() { return "PORTRAIT"; }
        @Override public String getPageFormat() { return "A4"; }
        @Override public String getTheme() { return null; }
        @Override public List<Integer> getMargins() { return Arrays.asList(20, 20, 20, 20); }
        @Override public List<GroupDefinition> getGroups() { return null; }
        @Override public List<ColumnDefinition> getColumns() { return null; }
        @Override public Map<String, ReportConfig> getSubreportConfigs() { return null; }
        @Override public boolean isPageFooterEnabled() { return false; }
        @Override public boolean isSummaryBandEnabled() { return false; }
        @Override public String getFooterLeftText() { return null; }
        @Override public pl.lib.model.CompanyInfo getCompanyInfo() { return null; }
    }
}
