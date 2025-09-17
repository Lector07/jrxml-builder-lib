package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ColumnDefinition;
import pl.lib.config.GroupDefinition;
import pl.lib.config.ReportConfig;
import pl.lib.config.ReportTheme;
import pl.lib.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON data report generator with full subreport support.
 *
 * <p>Class responsible for automatic JSON data structure analysis,
 * conversion to JasperReports format and generation of complete reports
 * with support for groups, calculations and hierarchical subreports.</p>
 *
 * <h3>Main functionalities:</h3>
 * <ul>
 *   <li>Automatic JSON structure analysis and data type detection</li>
 *   <li>Recursive subreport generation for nested data</li>
 *   <li>Data grouping and sorting support</li>
 *   <li>JSON data type conversion to Java/JasperReports types</li>
 *   <li>Optional debugging with JRXML output</li>
 *   <li>ISO 8601 date format support</li>
 *   <li>Automatic detection and handling of nested structures</li>
 * </ul>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * JsonReportGenerator generator = new JsonReportGenerator()
 *     .withJrxmlPrinting(true); // Optional debugging
 *
 * String jsonData = "[{\"name\":\"Product 1\",\"price\":100.50,\"details\":[...]}]";
 * ReportConfig config = ...; // Report configuration
 *
 * JasperPrint jasperPrint = generator.generateReportFromJson(jsonData, config);
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see AutomatedReportService
 * @see ReportConfig
 * @see ReportBuilder
 */
public class JsonReportGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean printJrxmlToConsole = false;
    private JasperDesign lastGeneratedDesign;

    /**
     * Configures the generator to print JRXML to console for debugging purposes.
     *
     * @param print true to enable JRXML printing
     * @return this generator (for method chaining)
     */
    public JsonReportGenerator withJrxmlPrinting(boolean print) {
        this.printJrxmlToConsole = print;
        return this;
    }

    public JasperDesign getLastGeneratedDesign() {
        return this.lastGeneratedDesign;
    }

    /**
     * Generates JasperReports report from JSON data.
     *
     * <p>Main method that parses JSON, analyzes data structure and generates
     * complete report with subreports and grouping.</p>
     *
     * @param jsonContent source data in JSON format (must be an array of objects)
     * @param config report configuration
     * @return JasperPrint object ready for export
     * @throws JRException in case of report compilation errors
     * @throws IOException in case of JSON parsing errors
     * @throws IllegalArgumentException if JSON is not an array
     */
    public JasperPrint generateReportFromJson(String jsonContent, ReportConfig config) throws JRException, IOException {
        JsonNode arrayNode = objectMapper.readTree(jsonContent);
        if (!arrayNode.isArray()) {
            throw new IllegalArgumentException("Provided JSON content must be a JSON array.");
        }
        return generateReportFromArray(arrayNode, config);
    }

    /**
     * Generates report from JSON node representing an array.
     *
     * @param arrayNode JSON node with array data
     * @param config report configuration
     * @return JasperPrint object
     * @throws JRException in case of compilation errors
     */
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

    /**
     * Compiles all subreports defined in configuration.
     *
     * @param structure main report data structure
     * @param config report configuration
     * @param isLandscape whether report has landscape orientation
     * @return map of compiled subreports
     * @throws JRException in case of compilation errors
     */
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
                    subBuilder.withHorizontalLayout(isLandscape);
                    subBuilder.withPageFormat(subConfig.getPageFormat());
                    subBuilder.withColorSettings(subConfig.getColorSettings());


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


    /**
     * Creates main report or subreport based on structure and configuration.
     *
     * @param builder report builder
     * @param structure data structure
     * @param config report configuration
     * @param compiledSubreports map of compiled subreports
     * @return compiled JasperReports report
     * @throws JRException in case of compilation errors
     */
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
                    boolean showSummary = subConfig != null && subConfig.isSummaryBandEnabled();
                    builder.addSubreport(new Subreport(fieldName, compiledSubreport, 50, subConfig.isSummaryBandEnabled()));
                }
            }
        }

        return builder.build();
    }

    /**
     * Analyzes JSON array structure and detects data types.
     *
     * <p>Recursively goes through all array elements and builds
     * data type map and detects nested structures.</p>
     *
     * @param arrayNode JSON node with array
     * @return structure describing the data
     */
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

    /**
     * Flattens JSON node to hierarchical structure.
     *
     * <p>Converts nested JSON objects to flat structure with field names
     * using dot notation (e.g. "address.city").</p>
     *
     * @param currentPath current path in hierarchy
     * @param jsonNode node to flatten
     * @param structure result structure
     */
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

    /**
     * Converts JSON array to list of maps for JasperReports.
     *
     * @param arrayNode JSON node with array
     * @return list of maps representing records
     */
    private List<Map<String, Object>> convertJsonArrayToList(JsonNode arrayNode) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                result.add(flattenJson(item));
            }
        }
        return result;
    }

    /**
     * Flattens single JSON object to map.
     *
     * @param node JSON node
     * @return map with object data
     */
    private Map<String, Object> flattenJson(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        addKeys("", node, map);
        return map;
    }

    /**
     * Adds keys from JSON node to result map.
     *
     * @param currentPath current path
     * @param jsonNode JSON node
     * @param map result map
     */
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

    /**
     * Converts JSON value to appropriate Java type.
     *
     * <p>Automatically detects data types and converts to appropriate Java classes,
     * including support for dates in ISO 8601 format.</p>
     *
     * @param value JSON node with value
     * @return converted Java value
     */
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

    /**
     * Determines data type based on JSON node.
     *
     * @param node JSON node
     * @return appropriate data type
     */
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

    /**
     * Applies the visual theme to the report based on configuration.
     * Uses the theme specified in config, or DEFAULT if not specified.
     *
     * @param builder report builder
     * @param config report configuration containing theme setting
     */
    private void addDefaultStyles(ReportBuilder builder, ReportConfig config) {
        // Determine which theme to use
        ReportTheme themeToApply = ReportTheme.DEFAULT; // default fallback

        if (config.getTheme() != null && !config.getTheme().trim().isEmpty()) {
            try {
                // Convert string theme name to enum
                themeToApply = ReportTheme.valueOf(config.getTheme().toUpperCase());
            } catch (IllegalArgumentException e) {
                // If invalid theme name, log warning and use default
                System.err.println("Warning: Invalid theme '" + config.getTheme() + "'. Using DEFAULT theme.");
                themeToApply = ReportTheme.DEFAULT;
            }
        }

        // Apply the determined theme
        builder.withTheme(themeToApply);
    }

    /**
     * Prints report JRXML to console for debugging purposes.
     *
     * @param report compiled report
     * @param reportName report name for identification
     */
    private void printJrxmlToConsole(JasperReport report, String reportName) {
        System.out.println("\n" + "=".repeat(80) + "\n=== " + reportName + " ===\n" + "=".repeat(80));
        System.out.println(JRXmlWriter.writeReport(report, "UTF-8"));
        System.out.println("=".repeat(80) + "\n");

    }

    /**
     * Internal class representing report data structure.
     *
     * <p>Stores information about fields, data types and nested structures
     * detected during JSON analysis.</p>
     */
    private static class ReportStructure {
        private Set<String> fields = new LinkedHashSet<>();
        private Map<String, DataType> fieldTypes = new HashMap<>();
        private Map<String, ReportStructure> nestedStructures = new HashMap<>();

        /**
         * Returns set of field names.
         *
         * @return set of field names
         */
        public Set<String> getFields() {
            return fields;
        }

        /**
         * Sets field names set.
         *
         * @param fields set of field names
         */
        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        /**
         * Returns map of data types for fields.
         *
         * @return data types map
         */
        public Map<String, DataType> getFieldTypes() {
            return fieldTypes;
        }

        /**
         * Sets data types map for fields.
         *
         * @param fieldTypes data types map
         */
        public void setFieldTypes(Map<String, DataType> fieldTypes) {
            this.fieldTypes = fieldTypes;
        }

        /**
         * Returns map of nested structures.
         *
         * @return nested structures map
         */
        public Map<String, ReportStructure> getNestedStructures() {
            return nestedStructures;
        }

        /**
         * Sets nested structures map.
         *
         * @param nestedStructures nested structures map
         */
        public void setNestedStructures(Map<String, ReportStructure> nestedStructures) {
            this.nestedStructures = nestedStructures;
        }
    }
}
