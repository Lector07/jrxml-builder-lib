package pl.lib.automation.analyzer;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class JsonStructureAnalyzer {
    public List<ReportElement> flattenJson(JsonNode rootNode) {
        List<ReportElement> elements = new ArrayList<>();
        rootNode.fields().forEachRemaining(entry ->
                flattenNodeRecursive(entry.getValue(), elements, 1, entry.getKey())
        );
        return elements;
    }



    private void flattenNodeRecursive(JsonNode node, List<ReportElement> elements, int level, String key) {
        if (node.isObject()) {
            // Sprawdź czy to jest wykres (ma pola: type, data, title)
            if (isChartNode(node)) {
                pl.lib.config.ChartConfig chartConfig = parseChartConfig(node);
                JsonNode dataNode = node.get("data");
                elements.add(ReportElement.createChart(key, level, dataNode, chartConfig));
            } else {
                elements.add(ReportElement.createHeader(key, level));
                node.fields().forEachRemaining(entry ->
                        flattenNodeRecursive(entry.getValue(), elements, level + 1, entry.getKey())
                );
            }
        } else if (node.isArray() && !node.isEmpty() && node.get(0).isObject()) {
            elements.add(ReportElement.createTable(key, level, node));
        } else if (node.isValueNode()) {
            elements.add(ReportElement.createKeyValue(key, node.asText("null"), level));
        }
    }

    private boolean isChartNode(JsonNode node) {
        return node.has("type") && node.has("data") && node.has("title");
    }

    private pl.lib.config.ChartConfig parseChartConfig(JsonNode node) {
        String type = node.get("type").asText();
        String title = node.get("title").asText();

        pl.lib.config.ChartConfig config = new pl.lib.config.ChartConfig();
        config.setTitle(title);

        // Mapowanie typu wykresu ze stringa na enum
        switch (type.toLowerCase()) {
            case "pie":
                config.setType(pl.lib.model.ChartType.PIE);
                break;
            case "bar":
                config.setType(pl.lib.model.ChartType.BAR);
                break;
            case "line":
                config.setType(pl.lib.model.ChartType.LINE);
                break;
            default:
                config.setType(pl.lib.model.ChartType.BAR);
        }

        return config;
    }
}
