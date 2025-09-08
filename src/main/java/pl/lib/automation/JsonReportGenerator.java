package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ColumnDefinition;
import pl.lib.config.GroupDefinition;
import pl.lib.config.ReportConfig;
import pl.lib.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OSTATECZNA WERSJA z pełną i poprawną obsługą PODRAPORTÓW.
 */
public class JsonReportGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean printJrxmlToConsole = false;

    public JsonReportGenerator withJrxmlPrinting(boolean print) {
        this.printJrxmlToConsole = print;
        return this;
    }

    public JasperPrint generateReportFromJson(String jsonContent, ReportConfig config) throws JRException, IOException {
        JsonNode arrayNode = objectMapper.readTree(jsonContent);
        if (!arrayNode.isArray()) {
            throw new IllegalArgumentException("Provided JSON content must be a JSON array.");
        }
        return generateReportFromArray(arrayNode, config);
    }

    private JasperPrint generateReportFromArray(JsonNode arrayNode, ReportConfig config) throws JRException {
        ReportStructure structure = analyzeArrayStructure(arrayNode);

        ReportBuilder reportBuilder = new ReportBuilder();

        if (config.getMargins() != null && config.getMargins().size() == 4) {
            reportBuilder.withMargins(config.getMargins().get(0), config.getMargins().get(1), config.getMargins().get(2), config.getMargins().get(3));
        } else {
            reportBuilder.withMargins(20, 20, 20, 20);
        }

        Map<String, JasperReport> compiledSubreports = compileSubreports(structure, config);
        for(Map.Entry<String, JasperReport> entry : compiledSubreports.entrySet()) {
            reportBuilder.getParameters().put("SUBREPORT_" + entry.getKey(), entry.getValue());
        }

        JasperReport mainReport = createMainReport(reportBuilder, structure, config, compiledSubreports);

        if (printJrxmlToConsole) {
            printJrxmlToConsole(mainReport, "MAIN REPORT: " + config.getTitle());
        }

        List<Map<String, Object>> mainData = convertJsonArrayToList(arrayNode);

        if (config.getGroups() != null && !config.getGroups().isEmpty()) {
            mainData.sort((map1, map2) -> {
                for (GroupDefinition groupDef : config.getGroups()) {
                    String field = groupDef.getField();
                    Object val1 = map1.get(field);
                    Object val2 = map2.get(field);
                    if (val1 == null && val2 == null) continue;
                    if (val1 == null) return -1;
                    if (val2 == null) return 1;
                    if (val1 instanceof Comparable) {
                        @SuppressWarnings("unchecked")
                        int result = ((Comparable<Object>) val1).compareTo(val2);
                        if (result != 0) return result;
                    }
                }
                return 0;
            });
        }

        JRDataSource dataSource = new JRMapCollectionDataSource((Collection<Map<String, ?>>) (Collection<?>) mainData);

        Map<String, Object> parameters = reportBuilder.getParameters();
        parameters.put("ReportTitle", config.getTitle());

        if (config.getCompanyInfo() != null) {
            parameters.put("CompanyName", config.getCompanyInfo().getName());
            parameters.put("CompanyAddress", config.getCompanyInfo().getAddress());
            parameters.put("CompanyPostalCode", config.getCompanyInfo().getPostalCode());
            parameters.put("CompanyCity", config.getCompanyInfo().getCity());
        }
        if (config.getFooterLeftText() != null) {
            parameters.put("FooterLeftText", config.getFooterLeftText());
        }

        return JasperFillManager.fillReport(mainReport, parameters, dataSource);
    }

    private Map<String, JasperReport> compileSubreports(ReportStructure structure, ReportConfig config) throws JRException {
        Map<String, JasperReport> compiledSubreports = new HashMap<>();
        if (config.getSubreportConfigs() != null) {
            for (Map.Entry<String, ReportConfig> entry : config.getSubreportConfigs().entrySet()) {
                String fieldName = entry.getKey();
                ReportConfig subConfig = entry.getValue();
                ReportStructure subStructure = structure.getNestedStructures().get(fieldName);
                if (subStructure != null) {
                    ReportBuilder subBuilder = new ReportBuilder("SUB_" + fieldName);

                    if (subConfig.getMargins() != null && subConfig.getMargins().size() == 4) {
                        subBuilder.withMargins(subConfig.getMargins().get(0), subConfig.getMargins().get(1), subConfig.getMargins().get(2), subConfig.getMargins().get(3));
                    } else {
                        subBuilder.withMargins(5, 20, 5, 20);
                    }

                    subBuilder.withTitleBand(false);
                    subBuilder.withPageFooter(false);
                    JasperReport compiledSubreport = createMainReport(subBuilder, subStructure, subConfig, new HashMap<>());
                    compiledSubreports.put(fieldName, compiledSubreport);
                }
            }
        }
        return compiledSubreports;
    }

    private JasperReport createMainReport(ReportBuilder builder, ReportStructure structure, ReportConfig config, Map<String, JasperReport> compiledSubreports) throws JRException {
        builder.withHorizontalLayout("LANDSCAPE".equalsIgnoreCase(config.getOrientation()))
                .withPageFooter(config.isPageFooterEnabled())
                .withFormattingOptions(config.getFormattingOptions());

        addDefaultStyles(builder);

        if (config.getGroups() != null) {
            for (GroupDefinition groupDef : config.getGroups()) {
                String labelExpression = (groupDef.getLabel() != null && !groupDef.getLabel().isEmpty())
                        ? groupDef.getLabel()
                        : "\"" + groupDef.getField() + ": \" + $F{" + groupDef.getField().replace('.', '_') + "}";
                builder.addGroup(new Group(groupDef.getField(), labelExpression, ReportStyles.GROUP_STYLE_1, groupDef.isShowFooter(), true));
            }
        }

        if (config.getColumns() != null) {
            for (ColumnDefinition colDef : config.getColumns()) {
                if (colDef.getVisible() != null && !colDef.getVisible()) continue;

                String fieldName = colDef.getField();

                if (config.getSubreportConfigs() != null && config.getSubreportConfigs().containsKey(fieldName)) {
                    continue;
                }

                DataType dataType = structure.getFieldTypes().getOrDefault(fieldName, DataType.STRING);
                builder.addColumn(new Column(
                        fieldName, colDef.getHeader(),
                        colDef.getWidth() != null ? colDef.getWidth() : -1,
                        dataType, colDef.getFormat(),
                        colDef.getReportCalculation() != null ? colDef.getReportCalculation() : Calculation.NONE,
                        colDef.getGroupCalculation() != null ? colDef.getGroupCalculation() : Calculation.NONE,
                        dataType.isNumeric() ? ReportStyles.NUMERIC_STYLE : ReportStyles.DATA_STYLE
                ));
            }
        }

        if (config.getSubreportConfigs() != null) {
            for (Map.Entry<String, ReportConfig> entry : config.getSubreportConfigs().entrySet()) {
                String fieldName = entry.getKey();
                JasperReport compiledSubreport = compiledSubreports.get(fieldName);
                if (compiledSubreport != null) {
                    builder.addSubreport(new Subreport(fieldName, compiledSubreport));
                }
            }
        }

        return builder.build();
    }

    private ReportStructure analyzeArrayStructure(JsonNode arrayNode) {
        ReportStructure structure = new ReportStructure();
        if (!arrayNode.isArray() || arrayNode.isEmpty()) return structure;
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                flattenNode("", item, structure);
            }
        }
        return structure;
    }

    private void flattenNode(String currentPath, JsonNode jsonNode, ReportStructure structure) {
        if (jsonNode.isObject()) {
            String prefix = currentPath.isEmpty() ? "" : currentPath + ".";
            jsonNode.fields().forEachRemaining(entry -> flattenNode(prefix + entry.getKey(), entry.getValue(), structure));
        } else if (jsonNode.isArray() && !currentPath.isEmpty()) {
            ReportStructure subStructure = analyzeArrayStructure(jsonNode);
            structure.getNestedStructures().put(currentPath, subStructure);
            structure.getFieldTypes().put(currentPath, DataType.JR_DATA_SOURCE);
            structure.getFields().add(currentPath);
        } else {
            structure.getFields().add(currentPath);
            structure.getFieldTypes().put(currentPath, determineDataType(jsonNode));
        }
    }

    private List<Map<String, Object>> convertJsonArrayToList(JsonNode arrayNode) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                result.add(flattenJson(item));
            }
        }
        return result;
    }

    private Map<String, Object> flattenJson(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        addKeys("", node, map);
        return map;
    }

    private void addKeys(String currentPath, JsonNode jsonNode, Map<String, Object> map) {
        if (jsonNode.isObject()) {
            String prefix = currentPath.isEmpty() ? "" : currentPath + ".";
            jsonNode.fields().forEachRemaining(entry -> addKeys(prefix + entry.getKey(), entry.getValue(), map));
        } else if (jsonNode.isArray()) {
            map.put(currentPath, new JRMapCollectionDataSource((Collection<Map<String, ?>>) (Collection<?>) convertJsonArrayToList(jsonNode)));
        } else {
            map.put(currentPath, convertJsonValue(jsonNode));
        }
    }

    private Object convertJsonValue(JsonNode value) {
        if (value == null || value.isNull()) return null;
        if (value.isTextual()) {
            try { return Date.from(Instant.parse(value.asText())); }
            catch (DateTimeParseException e) { return value.asText(); }
        }
        if (value.isNumber()) return new BigDecimal(value.asText());
        if (value.isBoolean()) return value.asBoolean();
        return value.toString();
    }

    private DataType determineDataType(JsonNode node) {
        if (node == null || node.isNull()) return DataType.STRING;
        if (node.isTextual()) {
            try { Instant.parse(node.asText()); return DataType.DATE; }
            catch (DateTimeParseException e) { return DataType.STRING; }
        }
        if (node.isNumber()) return DataType.BIG_DECIMAL;
        if (node.isBoolean()) return DataType.BOOLEAN;
        if (node.isArray()) return DataType.JR_DATA_SOURCE;
        return DataType.STRING;
    }

    private void addDefaultStyles(ReportBuilder builder) {
        String COLOR_HEADER_BACK = "#C6D8E4";
        String COLOR_GROUP_BACK = "#F0F0F0";
        String COLOR_HEADER_BORDER = "#DDDDDD";
        String COLOR_CELL_BORDER = "#D6D6D6";
        String COLOR_TEXT_BLACK = "#000000";

        builder.addStyle(new Style(ReportStyles.HEADER_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, true).withColors(COLOR_TEXT_BLACK, COLOR_HEADER_BACK).withAlignment("Center", "Middle").withBorders(1f, COLOR_HEADER_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.DATA_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(COLOR_TEXT_BLACK, null).withAlignment("Left", "Middle").withBorders(0.5f, COLOR_CELL_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.NUMERIC_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(COLOR_TEXT_BLACK, null).withAlignment("Right", "Middle").withBorders(0.5f, COLOR_CELL_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.GROUP_STYLE_1).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, true).withColors(COLOR_TEXT_BLACK, COLOR_GROUP_BACK).withAlignment("Left", "Middle").withBorders(0.5f, COLOR_CELL_BORDER).withPadding(3));
    }

    private void printJrxmlToConsole(JasperReport report, String reportName) {
        System.out.println("\n" + "=".repeat(80) + "\n=== " + reportName + " ===\n" + "=".repeat(80));
        System.out.println(JRXmlWriter.writeReport(report, "UTF-8"));
        System.out.println("=".repeat(80) + "\n");

    }

    private static class ReportStructure {
        private Set<String> fields = new LinkedHashSet<>();
        private Map<String, DataType> fieldTypes = new HashMap<>();
        private Map<String, ReportStructure> nestedStructures = new HashMap<>();

        public Set<String> getFields() { return fields; }
        public void setFields(Set<String> fields) { this.fields = fields; }
        public Map<String, DataType> getFieldTypes() { return fieldTypes; }
        public void setFieldTypes(Map<String, DataType> fieldTypes) { this.fieldTypes = fieldTypes; }
        public Map<String, ReportStructure> getNestedStructures() { return nestedStructures; }
        public void setNestedStructures(Map<String, ReportStructure> nestedStructures) { this.nestedStructures = nestedStructures; }
    }
}
