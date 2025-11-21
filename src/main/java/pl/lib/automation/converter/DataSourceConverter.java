package pl.lib.automation.converter;
import com.fasterxml.jackson.databind.JsonNode;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import pl.lib.automation.analyzer.ReportElement;
import java.util.*;
public class DataSourceConverter {
    public JRDataSource createMainDataSource(List<ReportElement> elements) {
        List<Map<String, ?>> dataSourceList = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            ReportElement el = elements.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("type", el.getType());
            map.put("text", el.getText());
            map.put("value", el.getValue());
            map.put("level", el.getLevel());
            map.put("elementIndex", i);
            dataSourceList.add(map);
        }
        return new JRMapCollectionDataSource(dataSourceList);
    }
    public JRDataSource createTableDataSource(JsonNode tableData) {
        List<Map<String, ?>> rows = new ArrayList<>();
        if (tableData.isArray()) {
            for (JsonNode row : tableData) {
                if (row.isObject()) {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    row.fields().forEachRemaining(entry -> {
                        String key = entry.getKey();
                        JsonNode value = entry.getValue();
                        rowMap.put(key, value.isNull() ? "" : value.asText());
                    });
                    rows.add(rowMap);
                }
            }
        }
        return new JRMapCollectionDataSource(rows);
    }

    public JRDataSource createChartDataSource(JsonNode chartData) {
        List<Map<String, ?>> dataPoints = new ArrayList<>();
        if (chartData != null && chartData.isObject()) {
            chartData.fields().forEachRemaining(entry -> {
                Map<String, Object> point = new HashMap<>();
                point.put("category", entry.getKey());
                point.put("value", entry.getValue().asDouble(0.0));
                dataPoints.add(point);
            });
        }
        return new JRMapCollectionDataSource(dataPoints);
    }
}
