package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.type.*;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ColumnDefinition;
import pl.lib.config.GroupDefinition;
import pl.lib.config.ReportConfig;
import pl.lib.config.ReportTheme;
import pl.lib.model.*;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

public class JsonReportGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean printJrxmlToConsole = false;
    private JasperDesign lastGeneratedDesign;

    private final Map<String, Object> reportParameters = new HashMap<>();

    public JsonReportGenerator withJrxmlPrinting(boolean print) {
        this.printJrxmlToConsole = print;
        return this;
    }

    public JasperDesign getLastGeneratedDesign() {
        return this.lastGeneratedDesign;
    }

    public JasperPrint generateReport(String jsonContent, String reportTitle) throws JRException, IOException {
        reportParameters.clear();

        ReportBuilder builder = new ReportBuilder(reportTitle)
                .withPageFormat("A4")
                .withHorizontalLayout(true)
                .withMargins(40, 40, 40, 40)
                .withTitle(reportTitle)
                .withTheme(ReportTheme.DEFAULT);

        JasperDesign design = builder.getDesign();
        design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);

        design.setProperty("net.sf.jasperreports.create.bookmarks", "true");

        JRDesignBand summaryBand = new JRDesignBand();
        summaryBand.setSplitType(SplitTypeEnum.STRETCH);
        summaryBand.setHeight(23);
        design.setSummary(summaryBand);

        JsonNode rootNode = objectMapper.readTree(jsonContent);
        processNode(summaryBand, design, reportTitle, rootNode, 0);

        JasperReport jasperReport = builder.build();
        this.lastGeneratedDesign = design;

        if (printJrxmlToConsole) {
            printJrxmlToConsole(jasperReport, "MAIN DYNAMIC REPORT");
        }

        return JasperFillManager.fillReport(jasperReport, this.reportParameters, new JREmptyDataSource(1));
    }

    public JasperPrint generateTableReportFromJson(String jsonContent, ReportConfig config) throws JRException, IOException {
        JsonNode arrayNode = objectMapper.readTree(jsonContent);
        if (!arrayNode.isArray()) {
            throw new IllegalArgumentException("Provided JSON content must be a JSON array for this method.");
        }
        return generateReportFromArray(arrayNode, config);
    }

    private void processNode(JRDesignBand band, JasperDesign design, String key, JsonNode node, int level) throws JRException {
        if (node.isObject()) {
            if (level > 0) {
                band.addElement(createHeader(key, design.getColumnWidth(), level));
            }
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                processNode(band, design, field.getKey(), field.getValue(), level + 1);
            }
        } else if (node.isArray() && node.size() > 0 && node.get(0).isObject()) {
            buildComplexTable(band, design, key, node);
        } else if (node.isValueNode()) {
            band.addElement(createKeyValueField(key, node.asText(), design.getColumnWidth(), level));
        }
    }

    private void buildComplexTable(JRDesignBand band, JasperDesign mainDesign, String title, JsonNode dataNode) throws JRException {
        List<Map<String, Object>> tableData = convertJsonArrayToList(dataNode);
        if (tableData.isEmpty()) return;

        ReportBuilder tableBuilder = new ReportBuilder("sub_" + title.replaceAll("\\s+|[^a-zA-Z0-9]", ""))
                .withTheme(ReportTheme.DEFAULT)
                .withTitleBand(false)
                .withPageFooter(false);

        tableBuilder.withColumnWidth(mainDesign.getColumnWidth());

        Map<String, Object> firstRow = tableData.get(0);
        for (String fieldName : firstRow.keySet()) {
            DataType type = determineDataType(firstRow.get(fieldName));
            String header = fieldName.substring(fieldName.lastIndexOf('_') + 1);

            tableBuilder.addColumn(new Column(
                    fieldName, header, -1,
                    type, type.isNumeric() ? "#,##0.00" : null,
                    Calculation.NONE, Calculation.NONE,
                    type.isNumeric() ? ReportStyles.NUMERIC_STYLE : ReportStyles.DATA_STYLE
            ));
        }

        tableBuilder.calculateColumnWidths();

        JasperReport compiledSubreport = tableBuilder.build();

        String subreportParamName = "SUB_" + UUID.randomUUID().toString().replace("-", "");
        String dataSourceParamName = "DATA_" + UUID.randomUUID().toString().replace("-", "");

        this.reportParameters.put(subreportParamName, compiledSubreport);
        this.reportParameters.put(dataSourceParamName, new JRMapCollectionDataSource((Collection<Map<String, ?>>) (Collection<?>) tableData));

        JRDesignParameter p1 = new JRDesignParameter(); p1.setName(subreportParamName); p1.setValueClass(JasperReport.class);
        mainDesign.addParameter(p1);
        JRDesignParameter p2 = new JRDesignParameter(); p2.setName(dataSourceParamName); p2.setValueClass(JRDataSource.class);
        mainDesign.addParameter(p2);

        band.addElement(createHeader(title, mainDesign.getColumnWidth(), 1));

        JRDesignSubreport subreportElement = new JRDesignSubreport(mainDesign);
        subreportElement.setPositionType(PositionTypeEnum.FLOAT);
        subreportElement.setWidth(mainDesign.getColumnWidth());
        subreportElement.setHeight(1);
        subreportElement.setExpression(new JRDesignExpression("$P{" + subreportParamName + "}"));
        subreportElement.setDataSourceExpression(new JRDesignExpression("$P{" + dataSourceParamName + "}"));
        band.addElement(subreportElement);
    }

    private JRDesignTextField createHeader(String text, int width, int level) {
        JRDesignTextField header = new JRDesignTextField();
        header.setX(level * 15);
        header.setY(0);
        header.setWidth(width - (level * 15));
        header.setHeight(25 - level * 2);
        header.setPositionType(PositionTypeEnum.FLOAT);

        JRDesignExpression expression = new JRDesignExpression();
        expression.setText("\"" + text.replace("\"", "\\\"") + "\"");
        header.setExpression(expression);

        if (level > 0) {
            String anchorName = "bookmark_" + text.replaceAll("\\s+|[^a-zA-Z0-9]", "_") + "_" + level;

            JRDesignExpression anchorExpression = new JRDesignExpression();
            anchorExpression.setText("\"" + anchorName + "\"");
            header.setAnchorNameExpression(anchorExpression);
            header.getPropertiesMap().setProperty("net.sf.jasperreports.export.pdf.bookmark.level", String.valueOf(level));
        }

        header.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        header.setFontSize(Math.max(10f, 16f - level * 2));
        header.setBold(true);
        header.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        header.setMode(ModeEnum.OPAQUE);
        header.setBackcolor(new Color(235, 235, 235));

        return header;
    }



    private JRDesignTextField createKeyValueField(String key, String value, int width, int level) {
        JRDesignTextField textField = new JRDesignTextField();
        textField.setX(level * 15);
        textField.setY(0);
        textField.setWidth(width - (level * 15));
        textField.setHeight(15);
        textField.setPositionType(PositionTypeEnum.FLOAT);
        textField.setStretchWithOverflow(true);
        textField.setMarkup("html");
        textField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        textField.setFontSize(9f);
        textField.setExpression(new JRDesignExpression("\"<b>" + escapeJava(key) + ":</b> " + escapeJava(value) + "\""));
        return textField;
    }

    private String escapeJava(String s) {
        if (s == null) return "null";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }


    private JasperPrint generateReportFromArray(JsonNode arrayNode, ReportConfig config) throws JRException {
        ReportStructure structure = analyzeArrayStructure(arrayNode);
        ReportBuilder reportBuilder = new ReportBuilder();
        reportBuilder.withHorizontalLayout("LANDSCAPE".equalsIgnoreCase(config.getOrientation()));
        reportBuilder.withPageFormat(config.getPageFormat());
        reportBuilder.withColorSettings(config.getColorSettings());

        if (config.getMargins() != null && config.getMargins().size() == 4) {
            reportBuilder.withMargins(config.getMargins().get(0), config.getMargins().get(1), config.getMargins().get(2), config.getMargins().get(3));
        } else {
            reportBuilder.withMargins(20, 20, 20, 20);
        }

        int mainReportColumnWidth = reportBuilder.preparePageAndGetColumnWidth();
        Map<String, JasperReport> compiledSubreports = compileSubreports(structure, config, "LANDSCAPE".equalsIgnoreCase(config.getOrientation()), mainReportColumnWidth);
        for (Map.Entry<String, JasperReport> entry : compiledSubreports.entrySet()) {
            reportBuilder.getParameters().put("SUBREPORT_" + entry.getKey(), entry.getValue());
        }

        JasperReport mainReport = createMainReport(reportBuilder, structure, config, compiledSubreports);
        this.lastGeneratedDesign = reportBuilder.getDesign();

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
            parameters.put("CompanyTaxId", config.getCompanyInfo().getTaxId());
        }
        if (config.getFooterLeftText() != null) {
            parameters.put("FooterLeftText", config.getFooterLeftText());
        }

        return JasperFillManager.fillReport(mainReport, parameters, dataSource);
    }

    private Map<String, JasperReport> compileSubreports(ReportStructure structure, ReportConfig config, boolean isLandscape, int parentColumnWidth) throws JRException {
        Map<String, JasperReport> compiledSubreports = new HashMap<>();
        if (config.getSubreportConfigs() != null) {
            for (Map.Entry<String, ReportConfig> entry : config.getSubreportConfigs().entrySet()) {
                String fieldName = entry.getKey();
                ReportConfig subConfig = entry.getValue();
                ReportStructure subStructure = structure.getNestedStructures().get(fieldName);
                if (subStructure != null) {
                    ReportBuilder subBuilder = new ReportBuilder("SUB_" + fieldName);
                    subBuilder.withColumnWidth(parentColumnWidth);
                    subBuilder.withColorSettings(config.getColorSettings());
                    subBuilder.withPageFormat(config.getPageFormat());
                    subBuilder.withHorizontalLayout(isLandscape);

                    if (config.getTheme() != null) {
                        try {
                            subBuilder.withTheme(ReportTheme.valueOf(config.getTheme().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            subBuilder.withTheme(ReportTheme.DEFAULT);
                        }
                    }

                    if (subConfig.getMargins() != null && subConfig.getMargins().size() == 4) {
                        subBuilder.withMargins(subConfig.getMargins().get(0), subConfig.getMargins().get(1), subConfig.getMargins().get(2), subConfig.getMargins().get(3));
                    } else {
                        subBuilder.withMargins(5, 20, 5, 40);
                    }

                    subBuilder.withTitleBand(false);
                    subBuilder.withPageFooter(false);
                    subBuilder.withSummaryBand(false);
                    JasperReport compiledSubreport = createMainReport(subBuilder, subStructure, subConfig, new HashMap<>());
                    compiledSubreports.put(fieldName, compiledSubreport);
                }
            }
        }
        return compiledSubreports;
    }

    private JasperReport createMainReport(ReportBuilder builder, ReportStructure structure, ReportConfig config, Map<String, JasperReport> compiledSubreports) throws JRException {
        builder.withPageFooter(config.isPageFooterEnabled())
                .withSummaryBand(config.isSummaryBandEnabled())
                .withFormattingOptions(config.getFormattingOptions());

        addDefaultStyles(builder, config);

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
                    ReportConfig subConfig = config.getSubreportConfigs().get(fieldName);
                    builder.addSubreport(new Subreport(fieldName, compiledSubreport, 50, subConfig.isSummaryBandEnabled()));
                }
            }
        }
        return builder.build();
    }

    private ReportStructure analyzeArrayStructure(JsonNode arrayNode) {
        ReportStructure structure = new ReportStructure();
        if (arrayNode != null && arrayNode.isArray() && !arrayNode.isEmpty()) {
            for (JsonNode item : arrayNode) {
                if (item.isObject()) {
                    flattenNode("", item, structure);
                }
            }
        }
        return structure;
    }

    private void flattenNode(String currentPath, JsonNode jsonNode, ReportStructure structure) {
        if (jsonNode.isObject()) {
            String prefix = currentPath.isEmpty() ? "" : currentPath + "_";
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
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                if (item.isObject()) {
                    result.add(flattenJson(item));
                }
            }
        }
        return result;
    }

    private Map<String, Object> flattenJson(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        addKeys("", node, map);
        return map;
    }

    private void addKeys(String currentPath, JsonNode jsonNode, Map<String, Object> map) {
        if (jsonNode.isObject()) {
            String prefix = currentPath.isEmpty() ? "" : currentPath + "_";
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

    private DataType determineDataType(Object value) {
        if (value == null) return DataType.STRING;
        if (value instanceof Date) return DataType.DATE;
        if (value instanceof BigDecimal || value instanceof Number) return DataType.BIG_DECIMAL;
        if (value instanceof Boolean) return DataType.BOOLEAN;
        if (value instanceof JRDataSource) return DataType.JR_DATA_SOURCE;
        return DataType.STRING;
    }

    private void addDefaultStyles(ReportBuilder builder, ReportConfig config) {
        ReportTheme themeToApply = ReportTheme.DEFAULT;
        if (config.getTheme() != null && !config.getTheme().trim().isEmpty()) {
            try {
                themeToApply = ReportTheme.valueOf(config.getTheme().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid theme '" + config.getTheme() + "'. Using DEFAULT theme.");
            }
        }
        builder.withTheme(themeToApply);
    }

    private void printJrxmlToConsole(JasperReport report, String reportName) {
            System.out.println("\n" + "=".repeat(80) + "\n=== " + reportName + " ===\n" + "=".repeat(80));
            System.out.println(JRXmlWriter.writeReport(report, "UTF-8"));
            System.out.println("=".repeat(80) + "\n");
    }

    private static class ReportStructure {
        private final Set<String> fields = new LinkedHashSet<>();
        private final Map<String, DataType> fieldTypes = new HashMap<>();
        private final Map<String, ReportStructure> nestedStructures = new HashMap<>();

        public Set<String> getFields() { return fields; }
        public Map<String, DataType> getFieldTypes() { return fieldTypes; }
        public Map<String, ReportStructure> getNestedStructures() { return nestedStructures; }
    }
}