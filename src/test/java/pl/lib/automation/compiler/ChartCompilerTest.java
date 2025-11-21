package pl.lib.automation.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignChart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.lib.config.ChartConfig;
import pl.lib.model.ChartType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testy dla ChartCompiler
 */
class ChartCompilerTest {

    private ChartCompiler compiler;
    private ObjectMapper objectMapper;
    private JsonNode testData;

    @BeforeEach
    void setUp() throws Exception {
        compiler = new ChartCompiler();
        objectMapper = new ObjectMapper();

        String jsonData = """
            [
                {"kategoria": "A", "wartosc": 100},
                {"kategoria": "B", "wartosc": 200},
                {"kategoria": "C", "wartosc": 150}
            ]
            """;
        testData = objectMapper.readTree(jsonData);
    }

    @Test
    void shouldCompileBarChart() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .title("Test Bar Chart")
            .categoryField("kategoria")
            .valueField("wartosc")
            .width(500)
            .height(300)
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart).isNotNull();
        assertThat(chart.getChartType()).isEqualTo((byte) 3); // BAR chart type
        assertThat(chart.getWidth()).isEqualTo(500);
        assertThat(chart.getHeight()).isEqualTo(300);
        assertThat(chart.getShowLegend()).isTrue();
    }

    @Test
    void shouldCompilePieChart() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.PIE)
            .title("Test Pie Chart")
            .categoryField("kategoria")
            .valueField("wartosc")
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart).isNotNull();
        assertThat(chart.getChartType()).isEqualTo((byte) 9); // PIE = 9 (JRChart.CHART_TYPE_PIE)
    }

    @Test
    void shouldCompileLineChart() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.LINE)
            .title("Test Line Chart")
            .categoryField("kategoria")
            .valueField("wartosc")
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart).isNotNull();
        assertThat(chart.getChartType()).isEqualTo((byte) 7); // LINE = 7 (JRChart.CHART_TYPE_LINE)
    }

    @Test
    void shouldCompileAreaChart() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.AREA)
            .title("Test Area Chart")
            .categoryField("kategoria")
            .valueField("wartosc")
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart).isNotNull();
        assertThat(chart.getChartType()).isEqualTo((byte) 1); // AREA = 1 (JRChart.CHART_TYPE_AREA)
    }

    @Test
    void shouldCompileStackedBarChart() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.STACKED_BAR)
            .title("Test Stacked Bar Chart")
            .categoryField("kategoria")
            .valueField("wartosc")
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart).isNotNull();
        assertThat(chart.getChartType()).isEqualTo((byte) 12); // STACKEDBAR = 12 (JRChart.CHART_TYPE_STACKEDBAR)
    }

    @Test
    void shouldCompilePie3DChart() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.PIE)
            .title("Test 3D Pie Chart")
            .categoryField("kategoria")
            .valueField("wartosc")
            .show3D(true)
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart).isNotNull();
        assertThat(chart.getChartType()).isEqualTo((byte) 8); // PIE3D = 8 (JRChart.CHART_TYPE_PIE3D)
    }

    @Test
    void shouldUseDefaultWidthWhenConfigWidthIsZero() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .categoryField("kategoria")
            .valueField("wartosc")
            .width(0)
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart.getWidth()).isEqualTo(555);
    }

    @Test
    void shouldSetChartTitle() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .title("Mój Wykres")
            .categoryField("kategoria")
            .valueField("wartosc")
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart.getTitleExpression()).isNotNull();
        assertThat(chart.getTitleExpression().getText()).contains("Mój Wykres");
    }

    @Test
    void shouldHideLegendWhenConfigured() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .categoryField("kategoria")
            .valueField("wartosc")
            .showLegend(false)
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart.getShowLegend()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenConfigIsNull() {
        assertThatThrownBy(() -> compiler.compileChart(null, testData, 555))
            .isInstanceOf(JRException.class)
            .hasMessageContaining("ChartConfig cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenChartTypeIsNull() {
        ChartConfig config = new ChartConfig.Builder()
            .categoryField("kategoria")
            .valueField("wartosc")
            .build();

        assertThatThrownBy(() -> compiler.compileChart(config, testData, 555))
            .isInstanceOf(JRException.class)
            .hasMessageContaining("ChartType cannot be null");
    }

    @Test
    void shouldSetCustomDimensions() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.PIE)
            .categoryField("kategoria")
            .valueField("wartosc")
            .width(800)
            .height(600)
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart.getWidth()).isEqualTo(800);
        assertThat(chart.getHeight()).isEqualTo(600);
    }

    @Test
    void shouldCompileChartWithAxisLabels() throws JRException {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .categoryField("kategoria")
            .valueField("wartosc")
            .categoryAxisLabel("Kategoria")
            .valueAxisLabel("Wartość (PLN)")
            .build();

        JRDesignChart chart = compiler.compileChart(config, testData, 555);

        assertThat(chart).isNotNull();
        // Sprawdzenie, czy wykres został utworzony z odpowiednimi polami
        assertThat(chart.getPlot()).isNotNull();
    }
}

