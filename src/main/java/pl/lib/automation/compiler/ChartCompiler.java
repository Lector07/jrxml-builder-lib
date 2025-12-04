package pl.lib.automation.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import net.sf.jasperreports.charts.design.*;
import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import pl.lib.config.ChartConfig;
import pl.lib.model.ChartType;

public class ChartCompiler {

    public JRDesignChart compileChart(ChartConfig config, JsonNode data, int width) throws JRException {
        if (config == null) {
            throw new JRException("ChartConfig cannot be null");
        }
        if (config.getType() == null) {
            throw new JRException("ChartType cannot be null");
        }

        return switch (config.getType()) {
            case PIE -> compilePieChart(config, width);
            case LINE -> compileLineChart(config, width);
            case AREA -> compileAreaChart(config, width);
            case STACKED_BAR -> compileStackedBarChart(config, width);
            default -> compileBarChart(config, width);
        };
    }

    private JRDesignChart compileBarChart(ChartConfig config, int width) throws JRException {
        JRDesignChart chart = new JRDesignChart(null, JRChart.CHART_TYPE_BAR);
        configureChartBasics(chart, config, width);


        JRDesignCategoryDataset dataset = new JRDesignCategoryDataset(null);
        JRDesignCategorySeries series = new JRDesignCategorySeries();

        series.setSeriesExpression(createExpression("\"Wartości\""));
        series.setCategoryExpression(createExpression("$F{category}"));
        series.setValueExpression(createExpression("$F{value}"));

        dataset.addCategorySeries(series);
        chart.setDataset(dataset);

        JRDesignBarPlot plot = (JRDesignBarPlot) chart.getPlot();
        plot.setShowTickLabels(true);
        plot.setShowTickMarks(true);

        if (config.getCategoryAxisLabel() != null) {
            plot.setCategoryAxisLabelExpression(createExpression("\"" + config.getCategoryAxisLabel() + "\""));
        }
        if (config.getValueAxisLabel() != null) {
            plot.setValueAxisLabelExpression(createExpression("\"" + config.getValueAxisLabel() + "\""));
        }

        return chart;
    }

    private JRDesignChart compilePieChart(ChartConfig config, int width) throws JRException {
        JRDesignChart chart = new JRDesignChart(null,
            config.isShow3D() ? JRChart.CHART_TYPE_PIE3D : JRChart.CHART_TYPE_PIE);
        configureChartBasics(chart, config, width);

        // Dataset - używamy standardowych pól
        JRDesignPieDataset dataset = new JRDesignPieDataset(null);
        JRDesignPieSeries series = new JRDesignPieSeries();

        series.setKeyExpression(createExpression("$F{category}"));
        series.setValueExpression(createExpression("$F{value}"));

        dataset.addPieSeries(series);
        chart.setDataset(dataset);

        return chart;
    }

    private JRDesignChart compileLineChart(ChartConfig config, int width) throws JRException {
        JRDesignChart chart = new JRDesignChart(null, JRChart.CHART_TYPE_LINE);
        configureChartBasics(chart, config, width);

        JRDesignCategoryDataset dataset = new JRDesignCategoryDataset(null);
        JRDesignCategorySeries series = new JRDesignCategorySeries();

        series.setSeriesExpression(createExpression("\"Trend\""));
        series.setCategoryExpression(createExpression("$F{category}"));
        series.setValueExpression(createExpression("$F{value}"));

        dataset.addCategorySeries(series);
        chart.setDataset(dataset);


        JRDesignLinePlot plot = (JRDesignLinePlot) chart.getPlot();
        plot.setShowLines(true);
        plot.setShowShapes(true);

        if (config.getCategoryAxisLabel() != null) {
            plot.setCategoryAxisLabelExpression(createExpression("\"" + config.getCategoryAxisLabel() + "\""));
        }
        if (config.getValueAxisLabel() != null) {
            plot.setValueAxisLabelExpression(createExpression("\"" + config.getValueAxisLabel() + "\""));
        }

        return chart;
    }

    private JRDesignChart compileAreaChart(ChartConfig config, int width) throws JRException {
        JRDesignChart chart = new JRDesignChart(null, JRChart.CHART_TYPE_AREA);
        configureChartBasics(chart, config, width);

        JRDesignCategoryDataset dataset = new JRDesignCategoryDataset(null);
        JRDesignCategorySeries series = new JRDesignCategorySeries();

        series.setSeriesExpression(createExpression("\"Obszar\""));
        series.setCategoryExpression(createExpression("$F{category}"));
        series.setValueExpression(createExpression("$F{value}"));

        dataset.addCategorySeries(series);
        chart.setDataset(dataset);

        return chart;
    }

    private JRDesignChart compileStackedBarChart(ChartConfig config, int width) throws JRException {
        JRDesignChart chart = new JRDesignChart(null, JRChart.CHART_TYPE_STACKEDBAR);
        configureChartBasics(chart, config, width);

        // Dataset - używamy standardowych pól
        JRDesignCategoryDataset dataset = new JRDesignCategoryDataset(null);
        JRDesignCategorySeries series = new JRDesignCategorySeries();

        series.setSeriesExpression(createExpression("\"Serie\""));
        series.setCategoryExpression(createExpression("$F{category}"));
        series.setValueExpression(createExpression("$F{value}"));

        dataset.addCategorySeries(series);
        chart.setDataset(dataset);

        return chart;
    }

    private void configureChartBasics(JRDesignChart chart, ChartConfig config, int width) {
        chart.setWidth(config.getWidth() > 0 ? config.getWidth() : width);
        chart.setHeight(config.getHeight());
        chart.setShowLegend(config.isShowLegend());
        chart.setEvaluationTime(EvaluationTimeEnum.NOW);

        if (config.getTitle() != null && !config.getTitle().isEmpty()) {
            chart.setTitleExpression(createExpression("\"" + config.getTitle() + "\""));
        }
    }

    private JRDesignExpression createExpression(String text) {
        JRDesignExpression expression = new JRDesignExpression();
        expression.setText(text);
        return expression;
    }
}

