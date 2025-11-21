package pl.lib.automation.analyzer;
import com.fasterxml.jackson.databind.JsonNode;
import pl.lib.config.ChartConfig;
public class ReportElement {
    private final String type;
    private final String text;
    private final String value;
    private final int level;
    private final JsonNode rawTableData;
    private final ChartConfig chartConfig;
    private ReportElement(String type, String text, String value, int level, JsonNode rawTableData, ChartConfig chartConfig) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.level = level;
        this.rawTableData = rawTableData;
        this.chartConfig = chartConfig;
    }
    public static ReportElement createHeader(String text, int level) {
        return new ReportElement("HEADER", text, null, level, null, null);
    }
    public static ReportElement createKeyValue(String text, String value, int level) {
        return new ReportElement("KEY_VALUE", text, value, level, null, null);
    }
    public static ReportElement createTable(String text, int level, JsonNode tableData) {
        return new ReportElement("TABLE", text, null, level, tableData, null);
    }
    public static ReportElement createChart(String text, int level, JsonNode data, ChartConfig chartConfig) {
        return new ReportElement("CHART", text, null, level, data, chartConfig);
    }
    public String getType() {
        return type;
    }
    public String getText() {
        return text;
    }
    public String getValue() {
        return value;
    }
    public int getLevel() {
        return level;
    }
    public JsonNode getRawTableData() {
        return rawTableData;
    }
    public ChartConfig getChartConfig() {
        return chartConfig;
    }
    @Override
    public String toString() {
        return String.format("Typ: %-15s | Poziom: %d | Tekst: %s", type, level, text != null ? text : "ROOT");
    }
}
