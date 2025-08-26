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
            printJrxmlToConsole(mainReport, "RAPORT GŁÓWNY: " + reportTitle);
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
                    if (!fieldTypes.containsKey(fieldName)) {
                        fieldTypes.put(fieldName, determineDataType(fieldValue));
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

        // --- KLUCZOWA ZMIANA 1: WSZYSTKO CO JEST LICZBĄ, JEST TERAZ BIGDECIMAL ---
        if (node.isNumber()) {
            return DataType.BIG_DECIMAL;
        }
        // ---------------------------------------------------------------------

        if (node.isBoolean()) return DataType.BOOLEAN;

        String text = node.asText();
        if (text.matches("\\d{13}")) {
            return DataType.DATE;
        }
        return DataType.STRING;
    }

    private JasperReport createMainReport(ReportBuilder builder, ReportStructure structure, Map<String, JasperReport> compiledSubreports) throws JRException {
        builder.withPageSize(842, 595).withMargins(20, 20, 20, 20)
                .withCompanyInfo("BIURO USŁUG KOMPUTEROWYCH \"SOFTRES\" SP Z O.O", "ul. Zaciszna 44, 35-326 Rzeszów", "NIP: 8130335217", "Regon: 690037603");

        addDefaultStyles(builder);



        Set<String> groupFields = new HashSet<>(Arrays.asList("sectionSegment", "chapterSegment"));

        for (String fieldName : structure.getFields()) {
            if (groupFields.contains(fieldName)) continue;

            DataType dataType = structure.getFieldTypes().get(fieldName);
            boolean isSubreport = structure.getNestedStructures().containsKey(fieldName);

            if (!isSubreport) {
                builder.addColumn(createColumn(fieldName, dataType));
            } else {
                ReportStructure subreportStructure = structure.getNestedStructures().get(fieldName);
                JasperReport subreport = createSubreport(fieldName, subreportStructure, builder.getParameters());
                compiledSubreports.put(fieldName, subreport);

                String subreportObjectName = "SUBREPORT_OBJECT_" + fieldName;
                String dataSourceExpression = "$F{" + fieldName + "}";

                builder.addSubreport(new Subreport("DETAIL", subreport, dataSourceExpression).withSubreportObjectParameterName(subreportObjectName));
                builder.addColumn(new Column(fieldName, "", 0, DataType.JR_DATA_SOURCE, null, Calculation.NONE, null, null));
            }
        }
        return builder.build();
    }

    private JasperReport createSubreport(String fieldName, ReportStructure structure, Map<String, Object> parentParams) throws JRException {
        ReportBuilder subreportBuilder = new ReportBuilder("Subreport_" + fieldName)
                .withPageSize(595, 842)
                .withMargins(10, 10, 10, 10)
                .setForSubreport(true); // <<<--- DODAJ TĘ LINIĘ

        subreportBuilder.getParameters().putAll(parentParams);
        subreportBuilder.getParameters().put("ReportTitle", "Szczegóły: " + fieldName);
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
        String styleName = "DataStyle";

        if (dataType == DataType.BIG_DECIMAL) {
            pattern = "#,##0.00";
            styleName = "NumericStyle";
            width = 120;
            reportCalc = Calculation.SUM;
            groupCalc = Calculation.SUM;
        } else if (dataType == DataType.DATE) {
            pattern = "yyyy-MM-dd";
        }

        return new Column(fieldName, title, width, dataType, pattern, reportCalc, groupCalc, styleName);
    }

    private void addDefaultStyles(ReportBuilder builder) {
        builder.addStyle(new Style("HeaderStyle").withFont("DejaVu Sans", 10, true).withColors("#FFFFFF", "#2A3F54").withAlignment("Center", "Middle").withBorders(1f, "#000000").withPadding(3))
                .addStyle(new Style("DataStyle").withFont("DejaVu Sans", 8, false).withColors("#000000", null).withAlignment("Left", "Middle").withBorders(0.5f, "#CCCCCC").withPadding(4))
                .addStyle(new Style("NumericStyle").withFont("DejaVu Sans", 8, false).withColors("#000000", null).withAlignment("Right", "Middle").withBorders(0.5f, "#CCCCCC").withPadding(4))
                .addStyle(new Style("GroupStyle1").withFont("DejaVu Sans", 9, true).withColors("#FFFFFF", "#4F6A83").withAlignment("Left", "Middle").withBorders(0.5f, "#000000").withPadding(3))
                .addStyle(new Style("GroupStyle2").withFont("DejaVu Sans", 9, false).withColors("#000000", "#D0D8E0").withAlignment("Left", "Middle").withBorders(0.5f, "#000000").withPadding(3));
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

        // --- KLUCZOWA ZMIANA 2: ZAWSZE KONWERTUJ LICZBY DO BIGDECIMAL ---
        if (value.isNumber()) {
            return new BigDecimal(value.asText());
        }
        // -------------------------------------------------------------

        if (value.isBoolean()) return value.asBoolean();
        if (value.isTextual()) {
            String text = value.asText();
            if (text.matches("\\d{13}")) {
                try {
                    return new Date(Long.parseLong(text));
                } catch (NumberFormatException e) {
                    // Ignoruj
                }
            }
            return text;
        }
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
        public Set<String> getFields() { return fields; }
        public void setFields(Set<String> fields) { this.fields = fields; }
        public Map<String, DataType> getFieldTypes() { return fieldTypes; }
        public void setFieldTypes(Map<String, DataType> fieldTypes) { this.fieldTypes = fieldTypes; }
        public Map<String, ReportStructure> getNestedStructures() { return nestedStructures; }
        public void setNestedStructures(Map<String, ReportStructure> nestedStructures) { this.nestedStructures = nestedStructures; }
    }
}