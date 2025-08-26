package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import pl.lib.api.ReportBuilder;
import pl.lib.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

public class JsonReportGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean printJrxmlToConsole = false;

    public JsonReportGenerator withJrxmlPrinting(boolean print) {
        this.printJrxmlToConsole = print;
        return this;
    }

    public JasperPrint generateReportFromJson(String jsonContent, String reportTitle) throws JRException, IOException {
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        return generateReportFromArray(rootNode, reportTitle);
    }

    private JasperPrint generateReportFromArray(JsonNode arrayNode, String reportTitle) throws JRException {
        ReportStructure structure = analyzeArrayStructure(arrayNode);
        Map<String, JasperReport> compiledSubreports = new HashMap<>();

        ReportBuilder reportBuilder = new ReportBuilder();
        JasperReport mainReport = createMainReport(reportBuilder, structure, compiledSubreports);

        if (printJrxmlToConsole) {
            printJrxmlToConsole(mainReport, "MAIN REPORT: " + reportTitle);
        }

        List<Map<String, Object>> mainData = convertJsonArrayToList(arrayNode);
        JRDataSource dataSource = new JRBeanCollectionDataSource(mainData);

        Map<String, Object> parameters = reportBuilder.getParameters();
        parameters.put("ReportTitle", reportTitle);

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

                    // Upgrade type if a more specific one is found.
                    // Precedence: BIG_DECIMAL > DATE > BOOLEAN > STRING.
                    if (existingType == null) {
                        fieldTypes.put(fieldName, newType);
                    } else if (newType != existingType) {
                        if (newType == DataType.BIG_DECIMAL) {
                            fieldTypes.put(fieldName, DataType.BIG_DECIMAL); // Highest precedence
                        } else if (newType == DataType.DATE && existingType != DataType.BIG_DECIMAL) {
                            fieldTypes.put(fieldName, DataType.DATE); // Upgrade to date if not already a number
                        } else if (newType == DataType.BOOLEAN && existingType == DataType.STRING) {
                            fieldTypes.put(fieldName, DataType.BOOLEAN); // Upgrade from string to boolean
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

    // Task 1: Improved date detection, prioritizing ISO 8601 strings
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
            // Fallback for 13-digit timestamp
            if (node.isIntegralNumber() && node.asText().length() == 13) {
                return DataType.DATE;
            }
            return DataType.BIG_DECIMAL;
        }

        if (node.isBoolean()) return DataType.BOOLEAN;

        return DataType.STRING;
    }

    // Task 2, 4, 5: Refactored main report creation
    private JasperReport createMainReport(ReportBuilder builder, ReportStructure structure, Map<String, JasperReport> compiledSubreports) throws JRException {
        // Task 4: Use CompanyInfo object
        CompanyInfo companyInfo = new CompanyInfo("BIURO USŁUG KOMPUTEROWYCH \"SOFTRES\" SP Z O.O")
                .withAddress("ul. Zaciszna 44")
                .withLocation("35-326", "Rzeszów")
                .withTaxId("8130335217");

        builder.withHorizontalLayout()
                .withMargins(20, 20, 20, 20)
                .withCompanyInfo(companyInfo);

        addDefaultStyles(builder);

        // Task 5: Translate group headers and use style constants
        builder.addGroup(new Group("paragraphGroup", "\"Section: \" + $F{paragraphGroup}", ReportStyles.GROUP_STYLE_1, true));
        builder.addGroup(new Group("origin", "\"Source: \" + $F{origin}", ReportStyles.GROUP_STYLE_2, true));
        builder.addGroup(new Group("chapterSegment", "\"Chapter: \" + $F{chapterSegment}", ReportStyles.GROUP_STYLE_2, true));
        builder.addGroup(new Group("classificationSymbol", "\"Classification: \" + $F{classificationSymbol}", ReportStyles.GROUP_STYLE_2, true));

        Set<String> groupFields = new HashSet<>(Arrays.asList("paragraphGroup", "origin", "chapterSegment", "classificationSymbol"));

        for (String fieldName : structure.getFields()) {
            if (groupFields.contains(fieldName)) continue;

            DataType dataType = structure.getFieldTypes().get(fieldName);
            boolean isSubreport = structure.getNestedStructures().containsKey(fieldName);

            if (!isSubreport) {
                // Task 2: Width calculation is now delegated to ReportBuilder.
                builder.addColumn(createColumn(fieldName, dataType));
            } else {
                ReportStructure subreportStructure = structure.getNestedStructures().get(fieldName);
                JasperReport subreport = createSubreport(fieldName, subreportStructure);
                compiledSubreports.put(fieldName, subreport);
                String subreportObjectName = "SUBREPORT_OBJECT_" + fieldName;
                String dataSourceExpression = "$F{" + fieldName + "}";
                builder.addSubreport(new Subreport("DETAIL", subreport, dataSourceExpression).withSubreportObjectParameterName(subreportObjectName));
                builder.addColumn(new Column(fieldName, "", 0, DataType.JR_DATA_SOURCE, null, Calculation.NONE, null, null));
            }
        }

        // Task 2: Width calculation logic is removed. ReportBuilder.build() will now handle it.
        return builder.build();
    }

    private JasperReport createSubreport(String fieldName, ReportStructure structure) throws JRException {
        ReportBuilder subreportBuilder = new ReportBuilder("Subreport_" + fieldName).withTitle("d")
                .withMargins(0, 0, 0, 0)
                .setForSubreport(true);

        // Task 5: Translate title
        subreportBuilder.withTitle("Summary of Resolutions/Orders");
        addDefaultStyles(subreportBuilder);

        for (String subfieldName : structure.getFields()) {
            subreportBuilder.addColumn(createColumn(subfieldName, structure.getFieldTypes().get(subfieldName)));
        }

        return subreportBuilder.build();
    }

    // Task 2 & 3: Refactored column creation with style constants and suggested widths
    private Column createColumn(String fieldName, DataType dataType) {
        String title = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1).replaceAll("([A-Z])", " $1").trim();
        int width = -1; // Default to auto-width (flexible column)
        String pattern = null;
        Calculation reportCalc = Calculation.NONE;
        Calculation groupCalc = Calculation.NONE;
        String styleName = ReportStyles.DATA_STYLE;

        // Assign a suggested fixed width for specific data types
        if (dataType.isNumeric()) {
            pattern = ReportStyles.NUMERIC_PATTERN;
            styleName = ReportStyles.NUMERIC_STYLE;
            reportCalc = Calculation.SUM;
            groupCalc = Calculation.SUM;
            width = 80; // Suggested width for numbers
        } else if (dataType == DataType.DATE) {
            pattern = ReportStyles.DATE_PATTERN;
            width = 70; // Suggested width for dates
        } else if (dataType == DataType.BOOLEAN) {
            width = 50; // Suggested width for booleans
        }

        // All other columns will remain with `width = -1` for dynamic calculation.
        return new Column(fieldName, title, width, dataType, pattern, reportCalc, groupCalc, styleName);
    }

    // Task 3: Use constants from ReportStyles
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

    // Task 1: Improved date conversion to handle ISO strings and timestamps
    private Object convertJsonValue(JsonNode value) {
        if (value == null || value.isNull()) return null;

        if (value.isTextual()) {
            try {
                return Date.from(Instant.parse(value.asText()));
            } catch (DateTimeParseException e) {
                return value.asText(); // Not a date, return as plain text
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

    // Task 5: Translate console output
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
