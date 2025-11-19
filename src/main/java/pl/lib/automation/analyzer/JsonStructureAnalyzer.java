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
            elements.add(ReportElement.createHeader(key, level));
            node.fields().forEachRemaining(entry ->
                    flattenNodeRecursive(entry.getValue(), elements, level + 1, entry.getKey())
            );
        } else if (node.isArray() && !node.isEmpty() && node.get(0).isObject()) {
            elements.add(ReportElement.createTable(key, level, node));
        } else if (node.isValueNode()) {
            elements.add(ReportElement.createKeyValue(key, node.asText("null"), level));
        }
    }
}
