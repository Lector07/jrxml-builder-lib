package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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

public class JsonReportGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean printJrxmlToConsole = false;

    public JsonReportGenerator withJrxmlPrinting(boolean print) {
        this.printJrxmlToConsole = print;
        return this;
    }

    public JasperPrint generateReportFromJson(String jsonContent, ReportConfig config) throws JRException, IOException {
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        JsonNode arrayNode = rootNode;

        if (rootNode.isObject()) {
            com.fasterxml.jackson.databind.node.ArrayNode mergedArray = objectMapper.createArrayNode();

            Iterator<JsonNode> elements = rootNode.elements();
            while (elements.hasNext()) {
                JsonNode node = elements.next();
                if (node.isArray()) {
                    for (JsonNode item : node) {
                        mergedArray.add(item);
                    }
                }
            }
            arrayNode = mergedArray;
        }

        return generateReportFromArray(arrayNode, config);
    }

    private JasperPrint generateReportFromArray(JsonNode arrayNode, ReportConfig config) throws JRException, IOException {
        ReportStructure structure = analyzeArrayStructure(arrayNode);
        Map<String, JasperReport> compiledSubreports = new HashMap<>();

        ReportBuilder reportBuilder = new ReportBuilder();
        JasperReport mainReport = createMainReport(reportBuilder, structure, compiledSubreports, config);

        if (printJrxmlToConsole) {
            printJrxmlToConsole(mainReport, "MAIN REPORT: " + config.getTitle());
        }

        List<Map<String, Object>> mainData = convertJsonArrayToList(arrayNode);

        List<String> groupFields = config.getGroups().stream()
                .map(GroupDefinition::getField)
                .collect(Collectors.toList());

        if (!groupFields.isEmpty()) {
            mainData.sort(new Comparator<Map<String, Object>>() {
                private final java.util.regex.Pattern leadingDigits = java.util.regex.Pattern.compile("^\\d+");

                @Override
                public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                    for (String field : groupFields) {
                        Object val1 = map1.get(field);
                        Object val2 = map2.get(field);

                        if (val1 == null && val2 == null) continue;
                        if (val1 == null) return -1;
                        if (val2 == null) return 1;

                        int comparison;
                        if (val1 instanceof String && val2 instanceof String) {
                            String s1 = (String) val1;
                            String s2 = (String) val2;
                            java.util.regex.Matcher m1 = leadingDigits.matcher(s1);
                            java.util.regex.Matcher m2 = leadingDigits.matcher(s2);

                            if (m1.find() && m2.find()) {
                                comparison = Long.compare(Long.parseLong(m1.group()), Long.parseLong(m2.group()));
                                if (comparison == 0) {
                                    comparison = s1.compareTo(s2);
                                }
                            } else {
                                comparison = s1.compareTo(s2);
                            }
                        } else if (val1 instanceof Comparable) {
                            @SuppressWarnings("unchecked")
                            int result = ((Comparable<Object>) val1).compareTo(val2);
                            comparison = result;
                        } else {
                            comparison = 0;
                        }

                        if (comparison != 0) {
                            return comparison;
                        }
                    }
                    return 0;
                }
            });
        }

        JRDataSource dataSource = new JRBeanCollectionDataSource(mainData);

        Map<String, Object> parameters = reportBuilder.getParameters();
        parameters.put("ReportTitle", config.getTitle());

        for (Map.Entry<String, JasperReport> entry : compiledSubreports.entrySet()) {
            String subreportObjectName = "SUBREPORT_OBJECT_" + entry.getKey();
            parameters.put(subreportObjectName, entry.getValue());
        }

        return JasperFillManager.fillReport(mainReport, parameters, dataSource);
    }

    private ReportStructure analyzeArrayStructure(JsonNode arrayNode) {
        ReportStructure structure = new ReportStructure();
        if (!arrayNode.isArray() || arrayNode.isEmpty()) {
            return structure;
        }

        Set<String> allFields = new LinkedHashSet<>();
        Map<String, DataType> fieldTypes = new HashMap<>();
        Map<String, ReportStructure> nestedStructures = new HashMap<>();

        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                item.fieldNames().forEachRemaining(fieldName -> {
                    allFields.add(fieldName);
                    JsonNode fieldValue = item.get(fieldName);

                    DataType newType = determineDataType(fieldValue);
                    DataType existingType = fieldTypes.get(fieldName);

                    if (existingType == null) {
                        fieldTypes.put(fieldName, newType);
                    } else if (newType != existingType) {
                        if (newType == DataType.BIG_DECIMAL) {
                            fieldTypes.put(fieldName, DataType.BIG_DECIMAL);
                        } else if (newType == DataType.DATE && existingType != DataType.BIG_DECIMAL) {
                            fieldTypes.put(fieldName, DataType.DATE);
                        } else if (newType == DataType.BOOLEAN && existingType == DataType.STRING) {
                            fieldTypes.put(fieldName, DataType.BOOLEAN);
                        }
                    }

                    if (fieldValue.isArray() && !fieldValue.isEmpty()) {
                        nestedStructures.computeIfAbsent(fieldName, k -> analyzeArrayStructure(fieldValue));
                    }
                });
            }
        }
        structure.setFields(allFields);
        structure.setFieldTypes(fieldTypes);
        structure.setNestedStructures(nestedStructures);
        return structure;
    }

    private DataType determineDataType(JsonNode node) {
        if (node == null || node.isNull()) return DataType.STRING;

        if (node.isTextual()) {
            try {
                Instant.parse(node.asText());
                return DataType.DATE;
            } catch (DateTimeParseException e) {
                return DataType.STRING;
            }
        }

        if (node.isNumber()) {
            if (node.isIntegralNumber() && node.asText().length() == 13) {
                return DataType.DATE;
            }
            return DataType.BIG_DECIMAL;
        }

        if (node.isBoolean()) return DataType.BOOLEAN;

        return DataType.STRING;
    }

    private JasperReport createMainReport(ReportBuilder builder, ReportStructure structure,
                                          Map<String, JasperReport> compiledSubreports, ReportConfig config) throws JRException {

        builder.withTitle(config.getTitle())
                .withHorizontalLayout()
                .withMargins(20, 20, 20, 20)
                .withCompanyInfo(config.getCompanyInfo())
                .withPageFooter(true);

        addDefaultStyles(builder);

        for (GroupDefinition groupDef : config.getGroups()) {
            if (!groupDef.isShowHeader()) {
                continue;
            }
            String labelExpression = (groupDef.getLabel() != null && !groupDef.getLabel().isEmpty())
                    ? groupDef.getLabel()
                    : "\"" + groupDef.getField() + ": \" + $F{" + groupDef.getField() + "}";

            builder.addGroup(new Group(
                    groupDef.getField(),
                    labelExpression,
                    ReportStyles.GROUP_STYLE_1,
                    groupDef.isShowFooter(),
                    groupDef.isShowHeader()
            ));
        }

        for (ColumnDefinition colDef : config.getColumns()) {

            if (colDef.getVisible() != null && !colDef.getVisible()) {
                continue;
            }

            String fieldName = colDef.getField();
            DataType dataType = structure.getFieldTypes().get(fieldName);

            if (dataType == null) {
                dataType = DataType.STRING;
            }

            boolean isSubreport = structure.getNestedStructures().containsKey(fieldName);
            if (!isSubreport) {
                String header = colDef.getHeader();
                Integer width = colDef.getWidth() != null ? colDef.getWidth() : -1;
                String format = colDef.getFormat();
                Calculation reportCalc = colDef.getReportCalculation() != null ? colDef.getReportCalculation() : Calculation.NONE;
                Calculation groupCalc = colDef.getGroupCalculation() != null ? colDef.getGroupCalculation() : Calculation.NONE;
                String style = dataType.isNumeric() ? ReportStyles.NUMERIC_STYLE : ReportStyles.DATA_STYLE;

                builder.addColumn(new Column(fieldName, header, width, dataType, format, reportCalc, groupCalc, style));
            } else {
                ReportStructure subreportStructure = structure.getNestedStructures().get(fieldName);
                ReportConfig subConfig = (config.getSubreportConfigs() != null) ? config.getSubreportConfigs().get(fieldName) : null;
                if (subConfig == null) {
                    subConfig = new ReportConfig.Builder().title("").build();
                }
                JasperReport subreport = createSubreport(fieldName, subreportStructure, subConfig);
                compiledSubreports.put(fieldName, subreport);
                String subreportObjectName = "SUBREPORT_OBJECT_" + fieldName;
                String dataSourceExpression = "$F{" + fieldName + "}";
                builder.addSubreport(new Subreport("DETAIL", subreport, dataSourceExpression).withSubreportObjectParameterName(subreportObjectName));
                builder.addColumn(new Column(fieldName, "", 0, DataType.JR_DATA_SOURCE, null, Calculation.NONE, Calculation.NONE, null));
            }
        }


        return builder.build();
    }

    private JasperReport createSubreport(String fieldName, ReportStructure structure, ReportConfig config) throws JRException {
        String subreportTitle = (config.getTitle() != null && !config.getTitle().isEmpty())
                ? config.getTitle()
                : fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        ReportBuilder subreportBuilder = new ReportBuilder("Subreport_" + fieldName)
                .withTitle(subreportTitle)
                .setForSubreport(true)
                .withMargins(4, 0, 4, 0);

        addDefaultStyles(subreportBuilder);

        Map<String, ColumnDefinition> columnConfigMap = config.getColumns().stream()
                .collect(Collectors.toMap(ColumnDefinition::getField, def -> def));

        for (String subfieldName : structure.getFields()) {
            ColumnDefinition colDef = columnConfigMap.get(subfieldName);

            if (colDef != null && colDef.getVisible() != null && !colDef.getVisible()) {
                continue;
            }

            DataType dataType = structure.getFieldTypes().get(subfieldName);
            if (dataType == null) {
                continue;
            }

            Column defaultColumn = createColumn(subfieldName, dataType);

            String header = (colDef != null && colDef.getHeader() != null) ? colDef.getHeader() : defaultColumn.getTitle();
            int width = (colDef != null && colDef.getWidth() != null) ? colDef.getWidth() : defaultColumn.getWidth();
            String format = (colDef != null && colDef.getFormat() != null) ? colDef.getFormat() : defaultColumn.getPattern();
            Calculation reportCalc = (colDef != null && colDef.getReportCalculation() != null) ? colDef.getReportCalculation() : defaultColumn.getReportCalculation();
            Calculation groupCalc = (colDef != null && colDef.getGroupCalculation() != null) ? colDef.getGroupCalculation() : defaultColumn.getGroupCalculation();

            subreportBuilder.addColumn(new Column(subfieldName, header, width, dataType, format, reportCalc, groupCalc, defaultColumn.getStyleName(), config.isUseSubreportBorders()));
        }

        return subreportBuilder.build();
    }

    private Column createColumn(String fieldName, DataType dataType) {
        String title = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1).replaceAll("([A-Z])", " $1").trim();
        int width = -1;
        String pattern = null;
        Calculation reportCalc = Calculation.NONE;
        Calculation groupCalc = Calculation.NONE;
        String styleName = ReportStyles.DATA_STYLE;

        if (dataType.isNumeric()) {
            pattern = ReportStyles.NUMERIC_PATTERN;
            styleName = ReportStyles.NUMERIC_STYLE;
            reportCalc = Calculation.SUM;
            groupCalc = Calculation.SUM;
            width = 82;
        } else if (dataType == DataType.DATE) {
            pattern = ReportStyles.DATE_PATTERN;
            width = 70;
        } else if (dataType == DataType.BOOLEAN) {
            width = 50;
        }

        return new Column(fieldName, title, width, dataType, pattern, reportCalc, groupCalc, styleName);
    }

    private void addDefaultStyles(ReportBuilder builder) {
        String COLOR_HEADER_BACK = "#C6D8E4";
        String COLOR_GROUP_BACK = "#F0F0F0";
        String COLOR_HEADER_BORDER = "#DDDDDD";
        String COLOR_CELL_BORDER = "#D6D6D6";
        String COLOR_TEXT_BLACK = "#000000";

        builder.addStyle(new Style(ReportStyles.HEADER_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(COLOR_TEXT_BLACK, COLOR_HEADER_BACK).withAlignment("Center", "Middle").withBorders(1f, COLOR_HEADER_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.DATA_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(COLOR_TEXT_BLACK, null).withAlignment("Left", "Middle").withBorders(0.5f, COLOR_CELL_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.NUMERIC_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(COLOR_TEXT_BLACK, null).withAlignment("Right", "Middle").withBorders(0.5f, COLOR_CELL_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.GROUP_STYLE_1).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(COLOR_TEXT_BLACK, COLOR_GROUP_BACK).withAlignment("Left", "Middle").withBorders(0.5f, COLOR_CELL_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.GROUP_STYLE_2).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(COLOR_TEXT_BLACK, COLOR_GROUP_BACK).withAlignment("Left", "Middle").withBorders(0.5f, COLOR_CELL_BORDER).withPadding(3));
    }

    private List<Map<String, Object>> convertJsonArrayToList(JsonNode arrayNode) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                result.add(convertJsonObjectToMap(item));
            }
        }
        return result;
    }

    private Map<String, Object> convertJsonObjectToMap(JsonNode objectNode) {
        Map<String, Object> map = new HashMap<>();
        objectNode.fields().forEachRemaining(entry -> map.put(entry.getKey(), convertJsonValue(entry.getValue())));
        return map;
    }

    private Object convertJsonValue(JsonNode value) {
        if (value == null || value.isNull()) return null;

        if (value.isTextual()) {
            try {
                return Date.from(Instant.parse(value.asText()));
            } catch (DateTimeParseException e) {
                return value.asText();
            }
        }

        if (value.isNumber()) {
            if (value.isIntegralNumber() && value.asText().length() == 13) {
                return new Date(value.asLong());
            }
            return new BigDecimal(value.asText());
        }

        if (value.isBoolean()) return value.asBoolean();

        if (value.isArray()) {
            return new JRBeanCollectionDataSource(convertJsonArrayToList(value));
        }

        return value.toString();
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

        public Set<String> getFields() {
            return fields;
        }

        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        public Map<String, DataType> getFieldTypes() {
            return fieldTypes;
        }

        public void setFieldTypes(Map<String, DataType> fieldTypes) {
            this.fieldTypes = fieldTypes;
        }

        public Map<String, ReportStructure> getNestedStructures() {
            return nestedStructures;
        }

        public void setNestedStructures(Map<String, ReportStructure> nestedStructures) {
            this.nestedStructures = nestedStructures;
        }
    }
}
