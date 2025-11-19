package pl.lib.automation.analyzer;
import com.fasterxml.jackson.databind.JsonNode;
public class ReportElement {
    private final String type;
    private final String text;
    private final String value;
    private final int level;
    private final JsonNode rawTableData;
    private ReportElement(String type, String text, String value, int level, JsonNode rawTableData) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.level = level;
        this.rawTableData = rawTableData;
    }
    public static ReportElement createHeader(String text, int level) {
        return new ReportElement("HEADER", text, null, level, null);
    }
    public static ReportElement createKeyValue(String text, String value, int level) {
        return new ReportElement("KEY_VALUE", text, value, level, null);
    }
    public static ReportElement createTable(String text, int level, JsonNode tableData) {
        return new ReportElement("TABLE", text, null, level, tableData);
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
    @Override
    public String toString() {
        return String.format("Typ: %-15s | Poziom: %d | Tekst: %s", type, level, text != null ? text : "ROOT");
    }
}
