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
        return generateReportFromArray(rootNode, config);
    }

    private JasperPrint generateReportFromArray(JsonNode arrayNode, ReportConfig config) throws JRException, IOException { // IOException dodany dla spójności
        ReportStructure structure = analyzeArrayStructure(arrayNode);
        Map<String, JasperReport> compiledSubreports = new HashMap<>();

        ReportBuilder reportBuilder = new ReportBuilder();
        JasperReport mainReport = createMainReport(reportBuilder, structure, compiledSubreports, config);

        if (printJrxmlToConsole) {
            printJrxmlToConsole(mainReport, "MAIN REPORT: " + config.getTitle());
        }

        List<Map<String, Object>> mainData = convertJsonArrayToList(arrayNode);
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

        CompanyInfo companyInfo = new CompanyInfo("BIURO USŁUG KOMPUTEROWYCH \"SOFTRES\" SP Z O.O")
                .withAddress("ul. Zaciszna 44")
                .withLocation("35-326", "Rzeszów")
                .withTaxId("8130335217");

        builder.withTitle(config.getTitle())
                .withHorizontalLayout()
                .withMargins(20, 20, 20, 20)
                .withCompanyInfo(companyInfo);

        addDefaultStyles(builder);

        // Grupowanie sterowane w 100% przez config
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
                    groupDef.isShowFooter()
            ));
        }

        // Mapa konfiguracji kolumn (dla szybkiego dostępu po nazwie pola)
        Map<String, ColumnDefinition> columnConfigMap = config.getColumns().stream()
                .collect(Collectors.toMap(ColumnDefinition::getField, def -> def));

        // Zbiór pól użytych do grupowania (nie powtarzamy jako kolumn)
        Set<String> groupFields = config.getGroups().stream()
                .map(GroupDefinition::getField)
                .collect(Collectors.toSet());

        // Iterujemy po polach wykrytych z JSON, a nie tylko po tych z konfiguracji
        for (String fieldName : structure.getFields()) {
            // Pomiń pola będące kluczami grup
            if (groupFields.contains(fieldName)) {
                continue;
            }

            // Opcjonalna konfiguracja kolumny użytkownika
            ColumnDefinition colDef = columnConfigMap.get(fieldName);

            // Jeśli jawnie ukryta w configu -> pomiń
            Boolean visible = (colDef != null) ? colDef.getVisible() : null;
            if (Boolean.FALSE.equals(visible)) {
                continue;
            }

            // Typ pola z analizy JSON
            DataType dataType = structure.getFieldTypes().get(fieldName);
            if (dataType == null) {
                continue;
            }

            boolean isSubreport = structure.getNestedStructures().containsKey(fieldName);
            if (!isSubreport) {
                String header = (colDef != null && colDef.getHeader() != null)
                        ? colDef.getHeader()
                        : fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                Integer width = (colDef != null && colDef.getWidth() != null) ? colDef.getWidth() : -1;
                String format = (colDef != null) ? colDef.getFormat() : null;
                Calculation reportCalc = (colDef != null && colDef.getReportCalculation() != null)
                        ? colDef.getReportCalculation() : Calculation.NONE;
                Calculation groupCalc = (colDef != null && colDef.getGroupCalculation() != null)
                        ? colDef.getGroupCalculation() : Calculation.NONE;
                String style = dataType.isNumeric() ? ReportStyles.NUMERIC_STYLE : ReportStyles.DATA_STYLE;

                builder.addColumn(new Column(fieldName, header, width, dataType, format, reportCalc, groupCalc, style));
            } else {
                // Subraport: jak wcześniej
                ReportStructure subreportStructure = structure.getNestedStructures().get(fieldName);
                JasperReport subreport = createSubreport(fieldName, subreportStructure);
                compiledSubreports.put(fieldName, subreport);

                String subreportObjectName = "SUBREPORT_OBJECT_" + fieldName;
                String dataSourceExpression = "$F{" + fieldName + "}";

                builder.addSubreport(
                        new Subreport("DETAIL", subreport, dataSourceExpression)
                                .withSubreportObjectParameterName(subreportObjectName)
                );

                builder.addColumn(new Column(fieldName, "", 0, DataType.JR_DATA_SOURCE, null, Calculation.NONE, null, null));
            }
        }

        return builder.build();
    }

    private JasperReport createSubreport(String fieldName, ReportStructure structure) throws JRException {
        String subreportTitle = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1).replaceAll("([A-Z])", " $1").trim();

        ReportBuilder subreportBuilder = new ReportBuilder("Subreport_" + fieldName)
                .withMargins(0, 0, 0, 0)
                .setForSubreport(true);

        addDefaultStyles(subreportBuilder);

        for (String subfieldName : structure.getFields()) {
            subreportBuilder.addColumn(createColumn(subfieldName, structure.getFieldTypes().get(subfieldName)));
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
            width = 80;
        } else if (dataType == DataType.DATE) {
            pattern = ReportStyles.DATE_PATTERN;
            width = 70;
        } else if (dataType == DataType.BOOLEAN) {
            width = 50;
        }

        return new Column(fieldName, title, width, dataType, pattern, reportCalc, groupCalc, styleName);
    }

    private void addDefaultStyles(ReportBuilder builder) {
        builder.addStyle(new Style(ReportStyles.HEADER_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 10, true).withColors(ReportStyles.COLOR_WHITE, ReportStyles.COLOR_PRIMARY_BACKGROUND).withAlignment("Center", "Middle").withBorders(1f, ReportStyles.COLOR_BLACK).withPadding(3))
                .addStyle(new Style(ReportStyles.DATA_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(ReportStyles.COLOR_BLACK, null).withAlignment("Left", "Middle").withBorders(0.5f, ReportStyles.COLOR_TABLE_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.NUMERIC_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, false).withColors(ReportStyles.COLOR_BLACK, null).withAlignment("Right", "Middle").withBorders(0.5f, ReportStyles.COLOR_TABLE_BORDER).withPadding(3))
                .addStyle(new Style(ReportStyles.GROUP_STYLE_1).withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true).withColors(ReportStyles.COLOR_WHITE, ReportStyles.COLOR_SECONDARY_BACKGROUND).withAlignment("Left", "Middle").withBorders(0.5f, ReportStyles.COLOR_BLACK).withPadding(3))
                .addStyle(new Style(ReportStyles.GROUP_STYLE_2).withFont(ReportStyles.FONT_DEJAVU_SANS, 8, false).withColors(ReportStyles.COLOR_BLACK, ReportStyles.COLOR_GROUP_BACKGROUND).withAlignment("Left", "Middle").withBorders(0.5f, ReportStyles.COLOR_BLACK).withPadding(3));
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
