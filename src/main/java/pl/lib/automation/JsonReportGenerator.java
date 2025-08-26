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
import java.util.stream.Collectors;

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

                    DataType newType = determineDataType(fieldValue);
                    DataType existingType = fieldTypes.get(fieldName);

                    if (existingType == null || (existingType != DataType.BIG_DECIMAL && newType == DataType.BIG_DECIMAL)) {
                        fieldTypes.put(fieldName, newType);
                    } else if (existingType != DataType.DATE && newType == DataType.DATE) {
                        fieldTypes.put(fieldName, newType);
                    } else if (!fieldTypes.containsKey(fieldName)){
                        fieldTypes.put(fieldName, newType);
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

        if (node.isNumber()) {
            if (node.isIntegralNumber() && node.asText().length() == 13) {
                return DataType.DATE;
            }
            return DataType.BIG_DECIMAL;
        }
        if (node.isBoolean()) return DataType.BOOLEAN;

        return DataType.STRING;
    }


    // === GŁÓWNA ZMIANA 1: Logika obliczania szerokości przeniesiona tutaj ===
    private JasperReport createMainReport(ReportBuilder builder, ReportStructure structure, Map<String, JasperReport> compiledSubreports) throws JRException {
        builder.withHorizontalLayout()
                .withMargins(20, 20, 20, 20)
                .withCompanyInfo("BIURO USŁUG KOMPUTEROWYCH \"SOFTRES\" SP Z O.O", "ul. Zaciszna 44, 35-326 Rzeszów", "NIP: 8130335217", "Regon: 690037603");

        addDefaultStyles(builder);

        builder.addGroup(new Group("paragraphGroup", "\"Dział: \" + $F{paragraphGroup}", "GroupStyle1", true));
        builder.addGroup(new Group("origin", "\"Źródło: \" + $F{origin}", "GroupStyle2", true));
        builder.addGroup(new Group("chapterSegment", "\"Rozdział: \" + $F{chapterSegment}", "GroupStyle2", true));
        builder.addGroup(new Group("classificationSymbol", "\"Klasyfikacja: \" + $F{classificationSymbol}", "GroupStyle2", true));

        Set<String> groupFields = new HashSet<>(Arrays.asList("paragraphGroup", "origin", "chapterSegment", "classificationSymbol"));

        // Krok 1: Stwórz listę wszystkich kolumn, które mają być wyświetlone w raporcie głównym
        List<Column> mainReportColumns = new ArrayList<>();
        for (String fieldName : structure.getFields()) {
            if (groupFields.contains(fieldName)) continue;

            DataType dataType = structure.getFieldTypes().get(fieldName);
            boolean isSubreport = structure.getNestedStructures().containsKey(fieldName);

            if (!isSubreport) {
                mainReportColumns.add(createColumn(fieldName, dataType));
            } else {
                // Obsługa subraportu (niezmieniona)
                ReportStructure subreportStructure = structure.getNestedStructures().get(fieldName);
                JasperReport subreport = createSubreport(fieldName, subreportStructure, builder.getParameters());
                compiledSubreports.put(fieldName, subreport);
                String subreportObjectName = "SUBREPORT_OBJECT_" + fieldName;
                String dataSourceExpression = "$F{" + fieldName + "}";
                builder.addSubreport(new Subreport("DETAIL", subreport, dataSourceExpression).withSubreportObjectParameterName(subreportObjectName));
                builder.addColumn(new Column(fieldName, "", 0, DataType.JR_DATA_SOURCE, null, Calculation.NONE, null, null));
            }
        }

        // Krok 2: Automatyczne obliczanie szerokości kolumn
        int availableWidth = 802; // Szerokość strony (842) - marginesy (20 + 20)

        // Filtrujemy kolumny na te o stałej szerokości i te, które mają być elastyczne (width = -1)
        List<Column> fixedWidthColumns = mainReportColumns.stream().filter(c -> c.getWidth() > 0).collect(Collectors.toList());
        List<Column> proportionalColumns = mainReportColumns.stream().filter(c -> c.getWidth() < 0).collect(Collectors.toList());

        int fixedWidthTotal = fixedWidthColumns.stream().mapToInt(Column::getWidth).sum();

        if (!proportionalColumns.isEmpty()) {
            int remainingWidth = availableWidth - fixedWidthTotal;
            int widthPerColumn = (remainingWidth > 0) ? remainingWidth / proportionalColumns.size() : 100; // 100 jako fallback

            for (Column col : proportionalColumns) {
                col.setWidth(widthPerColumn);
            }
        }

        // Krok 3: Dodaj skonfigurowane kolumny do buildera
        for(Column col : mainReportColumns) {
            builder.addColumn(col);
        }

        return builder.build();
    }

    private JasperReport createSubreport(String fieldName, ReportStructure structure, Map<String, Object> parentParams) throws JRException {
        // Ta metoda nie wymaga zmian, ponieważ subraport jest budowany w ten sam sposób
        ReportBuilder subreportBuilder = new ReportBuilder("Subreport_" + fieldName)
                .withMargins(0, 0, 0, 0)
                .setForSubreport(true);

        subreportBuilder.withTitle("Zestawienie uchwał/zarządzeń");
        addDefaultStyles(subreportBuilder);


        for (String subfieldName : structure.getFields()) {
            subreportBuilder.addColumn(createColumn(subfieldName, structure.getFieldTypes().get(subfieldName)));
        }

        return subreportBuilder.build();
    }


    private Column createColumn(String fieldName, DataType dataType) {
        String title = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1).replaceAll("([A-Z])", " $1").trim();
        int width = -1; // -1 oznacza "oblicz automatycznie" (kolumna elastyczna)
        String pattern = null;
        Calculation reportCalc = Calculation.NONE;
        Calculation groupCalc = Calculation.NONE;
        String styleName = "DataStyle";

        // Przypisujemy stałą szerokość tylko tam, gdzie to ma sens
        if (dataType.isNumeric()) {
            pattern = "#,##0.00";
            styleName = "NumericStyle";
            reportCalc = Calculation.SUM;
            groupCalc = Calculation.SUM;
            width = 70; // Stała szerokość dla liczb
        } else if (dataType == DataType.DATE) {
            pattern = "dd-MM-yy";
            width = 70; // Stała szerokość dla dat
        } else if (dataType == DataType.BOOLEAN) {
            width = 50; // Stała szerokość dla wartości logicznych
        }

        // Dla specyficznych krótkich pól tekstowych również możemy ustawić stałą szerokość
        switch(fieldName) {
            case "financingSegment":
            case "unitSymbol":
                width = 40;
                break;
        }

        // Wszystkie inne kolumny (np. sectionSegment, classificationName)
        // pozostaną z `width = -1`, dzięki czemu ich szerokość zostanie obliczona dynamicznie.

        return new Column(fieldName, title, width, dataType, pattern, reportCalc, groupCalc, styleName);
    }

    // ... (reszta metod: addDefaultStyles, convertJsonArrayToList, etc. pozostaje bez zmian)
    private void addDefaultStyles(ReportBuilder builder) {
        builder.addStyle(new Style("HeaderStyle").withFont("DejaVu Sans", 10, true).withColors("#FFFFFF", "#2A3F54").withAlignment("Center", "Middle").withBorders(1f, "#000000").withPadding(3))
                .addStyle(new Style("DataStyle").withFont("DejaVu Sans", 7, false).withColors("#000000", null).withAlignment("Left", "Middle").withBorders(0.5f, "#CCCCCC").withPadding(3))
                .addStyle(new Style("NumericStyle").withFont("DejaVu Sans", 7, false).withColors("#000000", null).withAlignment("Right", "Middle").withBorders(0.5f, "#CCCCCC").withPadding(3))
                .addStyle(new Style("GroupStyle1").withFont("DejaVu Sans", 9, true).withColors("#FFFFFF", "#4F6A83").withAlignment("Left", "Middle").withBorders(0.5f, "#000000").withPadding(3))
                .addStyle(new Style("GroupStyle2").withFont("DejaVu Sans", 8, false).withColors("#000000", "#D0D8E0").withAlignment("Left", "Middle").withBorders(0.5f, "#000000").withPadding(3));
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

        if (value.isNumber()) {
            if (value.isIntegralNumber() && value.asText().length() == 13) {
                return new Date(value.asLong());
            }
            return new BigDecimal(value.asText());
        }
        if (value.isBoolean()) return value.asBoolean();
        if (value.isTextual()) return value.asText();
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