package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;
import pl.lib.automation.analyzer.ReportElement;
import pl.lib.config.ChartConfig;
import pl.lib.model.ChartType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test integracyjny dla raportów z wykresami
 */
class ChartReportIntegrationTest {

    @Test
    void shouldGenerateReportWithBarChart() throws Exception {
        String json = """
            {
              "wprowadzenie": {
                "tytul": "Raport sprzedaży Q4 2024",
                "autor": "System raportowania"
              },
              "sprzedaz_miesieczna": [
                {"miesiac": "Październik", "sprzedaz": 120000, "koszt": 80000},
                {"miesiac": "Listopad", "sprzedaz": 135000, "koszt": 85000},
                {"miesiac": "Grudzień", "sprzedaz": 150000, "koszt": 90000}
              ]
            }
            """;

        // Utworzenie konfiguracji wykresu
        ChartConfig chartConfig = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .title("Sprzedaż miesięczna Q4 2024")
            .categoryField("miesiac")
            .valueField("sprzedaz")
            .width(500)
            .height(300)
            .categoryAxisLabel("Miesiąc")
            .valueAxisLabel("Sprzedaż (PLN)")
            .build();

        // Utworzenie elementów raportu
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode chartData = rootNode.get("sprzedaz_miesieczna");

        List<ReportElement> elements = new ArrayList<>();
        elements.add(ReportElement.createHeader("Wprowadzenie", 1));
        elements.add(ReportElement.createKeyValue("Tytuł", "Raport sprzedaży Q4 2024", 2));
        elements.add(ReportElement.createHeader("Sprzedaż miesięczna", 1));
        elements.add(ReportElement.createChart("Wykres sprzedaży", 2, chartData, chartConfig));

        // Weryfikacja
        assertThat(elements).hasSize(4);
        assertThat(elements.get(3).getType()).isEqualTo("CHART");
        assertThat(elements.get(3).getChartConfig()).isNotNull();
        assertThat(elements.get(3).getChartConfig().getType()).isEqualTo(ChartType.BAR);
    }

    @Test
    void shouldGenerateReportWithPieChart() throws Exception {
        String json = """
            {
              "udzial_w_rynku": [
                {"firma": "Firma A", "udzial": 35},
                {"firma": "Firma B", "udzial": 28},
                {"firma": "Firma C", "udzial": 22},
                {"firma": "Inne", "udzial": 15}
              ]
            }
            """;

        ChartConfig chartConfig = new ChartConfig.Builder()
            .type(ChartType.PIE)
            .title("Udział w rynku")
            .categoryField("firma")
            .valueField("udzial")
            .show3D(true)
            .build();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode chartData = rootNode.get("udzial_w_rynku");

        ReportElement chartElement = ReportElement.createChart(
            "Udział firm w rynku",
            1,
            chartData,
            chartConfig
        );

        assertThat(chartElement.getType()).isEqualTo("CHART");
        assertThat(chartElement.getChartConfig().getType()).isEqualTo(ChartType.PIE);
        assertThat(chartElement.getChartConfig().isShow3D()).isTrue();
    }

    @Test
    void shouldGenerateReportWithLineChart() throws Exception {
        String json = """
            {
              "trend_wzrostu": [
                {"rok": "2020", "wzrost": 5.2},
                {"rok": "2021", "wzrost": 6.8},
                {"rok": "2022", "wzrost": 7.5},
                {"rok": "2023", "wzrost": 8.1},
                {"rok": "2024", "wzrost": 9.3}
              ]
            }
            """;

        ChartConfig chartConfig = new ChartConfig.Builder()
            .type(ChartType.LINE)
            .title("Trend wzrostu 2020-2024")
            .categoryField("rok")
            .valueField("wzrost")
            .categoryAxisLabel("Rok")
            .valueAxisLabel("Wzrost (%)")
            .height(250)
            .build();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode chartData = rootNode.get("trend_wzrostu");

        ReportElement chartElement = ReportElement.createChart(
            "Analiza trendu",
            1,
            chartData,
            chartConfig
        );

        assertThat(chartElement.getChartConfig().getType()).isEqualTo(ChartType.LINE);
        assertThat(chartElement.getChartConfig().getCategoryAxisLabel()).isEqualTo("Rok");
        assertThat(chartElement.getChartConfig().getValueAxisLabel()).isEqualTo("Wzrost (%)");
    }

    @Test
    void shouldGenerateReportWithMultipleCharts() throws Exception {
        String json = """
            {
              "przychody": [
                {"region": "Północ", "kwota": 1200000},
                {"region": "Południe", "kwota": 980000},
                {"region": "Wschód", "kwota": 1100000},
                {"region": "Zachód", "kwota": 850000}
              ],
              "kategorie_produktow": [
                {"kategoria": "Elektronika", "sprzedaz": 45},
                {"kategoria": "Odzież", "sprzedaz": 30},
                {"kategoria": "AGD", "sprzedaz": 25}
              ]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        // Wykres słupkowy dla przychodów
        ChartConfig barChart = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .title("Przychody według regionów")
            .categoryField("region")
            .valueField("kwota")
            .build();

        // Wykres kołowy dla kategorii
        ChartConfig pieChart = new ChartConfig.Builder()
            .type(ChartType.PIE)
            .title("Kategorie produktów")
            .categoryField("kategoria")
            .valueField("sprzedaz")
            .build();

        List<ReportElement> elements = new ArrayList<>();
        elements.add(ReportElement.createHeader("Analiza przychodów", 1));
        elements.add(ReportElement.createChart(
            "Przychody", 2,
            rootNode.get("przychody"),
            barChart
        ));
        elements.add(ReportElement.createHeader("Kategorie produktów", 1));
        elements.add(ReportElement.createChart(
            "Produkty", 2,
            rootNode.get("kategorie_produktow"),
            pieChart
        ));

        assertThat(elements).hasSize(4);
        assertThat(elements.stream().filter(e -> "CHART".equals(e.getType())).count()).isEqualTo(2);
    }

    @Test
    void shouldCreateChartConfigWithAllParameters() {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.AREA)
            .title("Test Chart")
            .categoryField("cat")
            .valueField("val")
            .width(600)
            .height(400)
            .showLegend(false)
            .show3D(true)
            .categoryAxisLabel("Kategoria")
            .valueAxisLabel("Wartość")
            .build();

        assertThat(config.getType()).isEqualTo(ChartType.AREA);
        assertThat(config.getTitle()).isEqualTo("Test Chart");
        assertThat(config.getCategoryField()).isEqualTo("cat");
        assertThat(config.getValueField()).isEqualTo("val");
        assertThat(config.getWidth()).isEqualTo(600);
        assertThat(config.getHeight()).isEqualTo(400);
        assertThat(config.isShowLegend()).isFalse();
        assertThat(config.isShow3D()).isTrue();
        assertThat(config.getCategoryAxisLabel()).isEqualTo("Kategoria");
        assertThat(config.getValueAxisLabel()).isEqualTo("Wartość");
    }

    @Test
    void shouldGenerateReportWithAreaChart() throws Exception {
        ChartConfig chartConfig = new ChartConfig.Builder()
            .type(ChartType.AREA)
            .title("Zmiana wartości w czasie")
            .categoryField("okres")
            .valueField("wartosc")
            .build();

        String json = """
            [
                {"okres": "Q1", "wartosc": 100},
                {"okres": "Q2", "wartosc": 120},
                {"okres": "Q3", "wartosc": 150},
                {"okres": "Q4", "wartosc": 180}
            ]
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode chartData = mapper.readTree(json);

        ReportElement element = ReportElement.createChart("Area", 1, chartData, chartConfig);

        assertThat(element.getChartConfig().getType()).isEqualTo(ChartType.AREA);
    }

    @Test
    void shouldGenerateReportWithStackedBarChart() throws Exception {
        ChartConfig chartConfig = new ChartConfig.Builder()
            .type(ChartType.STACKED_BAR)
            .title("Porównanie skumulowane")
            .categoryField("kategoria")
            .valueField("suma")
            .build();

        String json = """
            [
                {"kategoria": "A", "suma": 50},
                {"kategoria": "B", "suma": 75},
                {"kategoria": "C", "suma": 60}
            ]
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode chartData = mapper.readTree(json);

        ReportElement element = ReportElement.createChart("Stacked", 1, chartData, chartConfig);

        assertThat(element.getChartConfig().getType()).isEqualTo(ChartType.STACKED_BAR);
    }
}

