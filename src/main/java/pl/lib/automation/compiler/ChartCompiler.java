package pl.lib.automation.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import net.sf.jasperreports.charts.design.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import pl.lib.config.ChartConfig;

public class ChartCompiler {
    public JRDesignChart compileChart(ChartConfig config, JsonNode data, int width) {
        return switch (config.getType().toUpperCase()) {
            case "PIE" -> compilePieChart(config, data, width);
            case "BAR" -> compileBarChart(config, data, width);
            case "LINE" -> compileLineChart(config, data, width);
            default -> compileBarChart(config, data, width);
        };
    }

    private JRDesignChart compileBarChart(ChartConfig config, JsonNode data, int width) {
        JRDesignChart chart = new JRDesignChart(new JRDesignStyle().getDefaultStyleProvider(), JRDesignChart.CHART_TYPE_BAR);

        chart.setWidth(config.getWidth() > 0 ? config.getWidth() : width);
        chart.setHeight(config.getHeight());
        chart.setShowLegend(config.isShowLegend());

        if (config.getTitle() != null) {
            chart.setTitleExpression(new JRDesignExpression("\"" + config.getTitle() + "\""));
        }

        JRDesignCategoryDataset dataset = new JRDesignCategoryDataset(null);
        JRDesignCategorySeries series = new JRDesignCategorySeries();

        series.setSeriesExpression(new JRDesignExpression(data.toString()));
        series.setCategoryExpression(new JRDesignExpression("$F{" + config.getCategoryField() + "}"));
        series.setValueExpression(
                new JRDesignExpression("$F{" + config.getValueField() + "}")
        );

        dataset.addCategorySeries(series);
        chart.setDataset(dataset);

        JRDesignBarPlot plot = (JRDesignBarPlot) chart.getPlot();
        plot.setShowTickLabels(true);
        plot.setShowTickMarks(true);

        return chart;
    }

    private JRDesignChart compilePieChart(ChartConfig config, JsonNode data, int width) {
        JRDesignChart chart = new JRDesignChart(new JRDesignStyle().getDefaultStyleProvider(), JRDesignChart.CHART_TYPE_PIE);

        chart.setWidth(config.getWidth() > 0 ? config.getWidth() : width);
        chart.setHeight(config.getHeight());
        chart.setShowLegend(config.isShowLegend());
        if (config.getTitle() != null) {
            chart.setTitleExpression(new JRDesignExpression("\"" + config.getTitle() + "\""));
        }

        JRDesignPieDataset dataset = new JRDesignPieDataset(null);
        JRDesignPieSeries series = new JRDesignPieSeries();

        series.setKeyExpression(new JRDesignExpression("$F{" + config.getCategoryField() + "}"));
        series.setValueExpression(new JRDesignExpression("$F{" + config.getValueField() + "}"));

        dataset.addPieSeries(series);
        chart.setDataset(dataset);


        return chart;
    }

    private JRDesignChart compileLineChart(ChartConfig config, JsonNode data, int width) {
        JRDesignChart chart = new JRDesignChart(new JRDesignStyle().getDefaultStyleProvider(), JRDesignChart.CHART_TYPE_LINE);

        chart.setWidth(config.getWidth() > 0 ? config.getWidth() : width);
        chart.setHeight(config.getHeight());
        chart.setShowLegend(config.isShowLegend());
        if (config.getTitle() != null) {
            chart.setTitleExpression(new JRDesignExpression("\"" + config.getTitle() + "\""));
        }

        JRDesignCategoryDataset dataset = new JRDesignCategoryDataset(null);
        JRDesignCategorySeries series = new JRDesignCategorySeries();

        series.setSeriesExpression(new JRDesignExpression("\"Trend\""));
        series.setCategoryExpression(new JRDesignExpression("$F{" + config.getCategoryField() + "}"));
        series.setValueExpression(
                new JRDesignExpression("$F{" + config.getValueField() + "}")
        );


        dataset.addCategorySeries(series);
        chart.setDataset(dataset);

        return chart;
    }
}
