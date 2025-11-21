package pl.lib.config;

import org.junit.jupiter.api.Test;
import pl.lib.model.ChartType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy dla ChartConfig
 */
class ChartConfigTest {

    @Test
    void shouldCreateChartConfigWithDefaultValues() {
        ChartConfig config = new ChartConfig();

        assertThat(config.getWidth()).isEqualTo(500);
        assertThat(config.getHeight()).isEqualTo(300);
        assertThat(config.isShowLegend()).isTrue();
        assertThat(config.isShow3D()).isFalse();
    }

    @Test
    void shouldCreateChartConfigWithConstructor() {
        ChartConfig config = new ChartConfig(
            ChartType.BAR,
            "Test Chart",
            "category",
            "value"
        );

        assertThat(config.getType()).isEqualTo(ChartType.BAR);
        assertThat(config.getTitle()).isEqualTo("Test Chart");
        assertThat(config.getCategoryField()).isEqualTo("category");
        assertThat(config.getValueField()).isEqualTo("value");
    }

    @Test
    void shouldCreateChartConfigWithBuilder() {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.LINE)
            .title("Trend Sprzedaży")
            .categoryField("miesiac")
            .valueField("sprzedaz")
            .width(600)
            .height(400)
            .showLegend(false)
            .categoryAxisLabel("Miesiąc")
            .valueAxisLabel("PLN")
            .build();

        assertThat(config.getType()).isEqualTo(ChartType.LINE);
        assertThat(config.getTitle()).isEqualTo("Trend Sprzedaży");
        assertThat(config.getCategoryField()).isEqualTo("miesiac");
        assertThat(config.getValueField()).isEqualTo("sprzedaz");
        assertThat(config.getWidth()).isEqualTo(600);
        assertThat(config.getHeight()).isEqualTo(400);
        assertThat(config.isShowLegend()).isFalse();
        assertThat(config.getCategoryAxisLabel()).isEqualTo("Miesiąc");
        assertThat(config.getValueAxisLabel()).isEqualTo("PLN");
    }

    @Test
    void shouldSetAndGetAllProperties() {
        ChartConfig config = new ChartConfig();

        config.setType(ChartType.PIE);
        config.setTitle("Wykres Kołowy");
        config.setCategoryField("kategoria");
        config.setValueField("wartosc");
        config.setWidth(700);
        config.setHeight(500);
        config.setShowLegend(true);
        config.setShow3D(true);
        config.setCategoryAxisLabel("Kategoria");
        config.setValueAxisLabel("Wartość");

        assertThat(config.getType()).isEqualTo(ChartType.PIE);
        assertThat(config.getTitle()).isEqualTo("Wykres Kołowy");
        assertThat(config.getCategoryField()).isEqualTo("kategoria");
        assertThat(config.getValueField()).isEqualTo("wartosc");
        assertThat(config.getWidth()).isEqualTo(700);
        assertThat(config.getHeight()).isEqualTo(500);
        assertThat(config.isShowLegend()).isTrue();
        assertThat(config.isShow3D()).isTrue();
        assertThat(config.getCategoryAxisLabel()).isEqualTo("Kategoria");
        assertThat(config.getValueAxisLabel()).isEqualTo("Wartość");
    }

    @Test
    void shouldCreatePieChartConfig() {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.PIE)
            .title("Udział w rynku")
            .categoryField("firma")
            .valueField("udzial")
            .show3D(true)
            .build();

        assertThat(config.getType()).isEqualTo(ChartType.PIE);
        assertThat(config.isShow3D()).isTrue();
    }

    @Test
    void shouldCreateBarChartConfig() {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.BAR)
            .title("Przychody według regionów")
            .categoryField("region")
            .valueField("przychod")
            .categoryAxisLabel("Region")
            .valueAxisLabel("Przychód (PLN)")
            .build();

        assertThat(config.getType()).isEqualTo(ChartType.BAR);
        assertThat(config.getCategoryAxisLabel()).isEqualTo("Region");
        assertThat(config.getValueAxisLabel()).isEqualTo("Przychód (PLN)");
    }

    @Test
    void shouldCreateAreaChartConfig() {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.AREA)
            .title("Zmiana w czasie")
            .categoryField("data")
            .valueField("wartosc")
            .build();

        assertThat(config.getType()).isEqualTo(ChartType.AREA);
    }

    @Test
    void shouldCreateStackedBarChartConfig() {
        ChartConfig config = new ChartConfig.Builder()
            .type(ChartType.STACKED_BAR)
            .title("Porównanie skumulowane")
            .categoryField("kategoria")
            .valueField("suma")
            .build();

        assertThat(config.getType()).isEqualTo(ChartType.STACKED_BAR);
    }
}

