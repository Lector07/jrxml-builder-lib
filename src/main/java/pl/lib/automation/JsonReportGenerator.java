package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
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
    private final Map<String, Object> reportParameters = new HashMap<>();
    private boolean printJrxmlToConsole = false;
    private JasperDesign lastGeneratedDesign;

    public JsonReportGenerator withJrxmlPrinting(boolean print) {
        this.printJrxmlToConsole = print;
        return this;
    }

    public JasperDesign getLastGeneratedDesign() {
        return this.lastGeneratedDesign;
    }

    /**
     * Wydobywa nagłówki z JSON do utworzenia spisu treści.
     * Zwraca listę map z polami: label, level, pageNumber (oszacowany).
     */
    public List<Map<String, Object>> extractTocEntries(String jsonContent) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        List<ReportElement> reportElements = flattenJsonForDebugging(rootNode);

        List<Map<String, Object>> tocEntries = new ArrayList<>();
        int currentPage = 1; // Strona głównej treści (po stronie tytułowej i spisie treści)

        for (ReportElement element : reportElements) {
            if ("HEADER".equals(element.type)) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("label", element.text);
                entry.put("level", element.level);
                entry.put("pageNumber", currentPage + 2); // +2 bo strona tytułowa i spis treści
                tocEntries.add(entry);
            }
        }

        return tocEntries;
    }

//    public JasperPrint generateReport(String jsonContent, String reportTitle) throws JRException, IOException {
//        System.out.println("--- Rozpoczynam spłaszczanie JSON ---");
//
//        JsonNode rootNode = objectMapper.readTree(jsonContent);
//        List<ReportElement> reportElements = flattenJsonForDebugging(rootNode);
//
//        System.out.println("--- Wynik spłaszczania: ---");
//        reportElements.forEach(System.out::println);
//        System.out.println("--- Zakończono spłaszczanie ---");
//
//        return null;
//    }

//    public JasperPrint generateReport(String jsonContent, String reportTitle) throws JRException, IOException {
//        reportParameters.clear();
//
//        ReportBuilder builder = new ReportBuilder(reportTitle)
//                .withPageFormat("A4")
//                .withHorizontalLayout(true)
//                .withMargins(40, 40, 40, 40)
//                .withTitle(reportTitle)
//                .withTheme(ReportTheme.DEFAULT);
//
//        JasperDesign design = builder.getDesign();
//        design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
//
//        design.setProperty("net.sf.jasperreports.create.bookmarks", "true");
//
//        JRDesignBand summaryBand = new JRDesignBand();
//        summaryBand.setSplitType(SplitTypeEnum.STRETCH);
//        summaryBand.setHeight(23);
//        design.setSummary(summaryBand);
//
//        JsonNode rootNode = objectMapper.readTree(jsonContent);
//        processNode(summaryBand, design, reportTitle, rootNode, 0);
//
//        JasperReport jasperReport = builder.build();
//        this.lastGeneratedDesign = design;
//
//        if (printJrxmlToConsole) {
//            printJrxmlToConsole(jasperReport, "MAIN DYNAMIC REPORT");
//        }
//
//        return JasperFillManager.fillReport(jasperReport, this.reportParameters, new JREmptyDataSource(1));
//    }

    public JasperPrint generateReport(String jsonContent, String reportTitle) throws JRException, IOException {
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        List<ReportElement> reportElements = flattenJsonForDebugging(rootNode);

        ReportBuilder builder = new ReportBuilder(reportTitle)
                .withTheme(ReportTheme.DEFAULT)
                .withPageFormat("A4")
                .withHorizontalLayout(false)
                .withMargins(20, 20, 20, 20)
                .withTitleBand(false)
                .withSummaryBand(false);

        JasperDesign design = builder.getDesign();
        design.setProperty("net.sf.jasperreports.create.bookmarks", "true");

        // Krok 1: Prekompiluj wszystkie subreporty dla tabel
        Map<Integer, JasperReport> compiledTables = new HashMap<>();
        Map<Integer, JRDataSource> tableDataSources = new HashMap<>();

        for (int i = 0; i < reportElements.size(); i++) {
            ReportElement element = reportElements.get(i);
            if ("TABLE".equals(element.type) && element.rawTableData != null) {
                JasperReport tableReport = compileTableSubreport(element.rawTableData, design.getColumnWidth());
                JRDataSource tableData = createTableDataSource(element.rawTableData);

                String subreportParamName = "TABLE_REPORT_" + i;
                String dataSourceParamName = "TABLE_DATA_" + i;

                compiledTables.put(i, tableReport);
                tableDataSources.put(i, tableData);

                reportParameters.put(subreportParamName, tableReport);
                reportParameters.put(dataSourceParamName, tableData);

                // Dodaj parametry do designu
                JRDesignParameter p1 = new JRDesignParameter();
                p1.setName(subreportParamName);
                p1.setValueClass(JasperReport.class);
                design.addParameter(p1);

                JRDesignParameter p2 = new JRDesignParameter();
                p2.setName(dataSourceParamName);
                p2.setValueClass(JRDataSource.class);
                design.addParameter(p2);
            }
        }

        // Krok 2: Przygotuj źródło danych
        List<Map<String, ?>> dataSourceList = new ArrayList<>();
        for (int i = 0; i < reportElements.size(); i++) {
            ReportElement el = reportElements.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("type", el.type);
            map.put("text", el.text);
            map.put("value", el.value);
            map.put("level", el.level);
            map.put("elementIndex", i);
            dataSourceList.add(map);
        }
        JRDataSource dataSource = new JRMapCollectionDataSource(dataSourceList);

        // Krok 3: Dodaj pola
        JRDesignField fieldType = new JRDesignField();
        fieldType.setName("type");
        fieldType.setValueClass(String.class);
        design.addField(fieldType);

        JRDesignField fieldText = new JRDesignField();
        fieldText.setName("text");
        fieldText.setValueClass(String.class);
        design.addField(fieldText);

        JRDesignField fieldValue = new JRDesignField();
        fieldValue.setName("value");
        fieldValue.setValueClass(String.class);
        design.addField(fieldValue);

        JRDesignField fieldLevel = new JRDesignField();
        fieldLevel.setName("level");
        fieldLevel.setValueClass(Integer.class);
        design.addField(fieldLevel);

        JRDesignField fieldIndex = new JRDesignField();
        fieldIndex.setName("elementIndex");
        fieldIndex.setValueClass(Integer.class);
        design.addField(fieldIndex);

        // Krok 4: Zbuduj bandę detail
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(11); // Rozmiar największego elementu (nagłówka)
        detailBand.setSplitType(SplitTypeEnum.STRETCH);

        // Element 1: Nagłówek
        JRDesignTextField headerField = new JRDesignTextField();
        headerField.setX(0);
        headerField.setY(0);
        headerField.setWidth(design.getColumnWidth());
        headerField.setHeight(11);
        headerField.setRemoveLineWhenBlank(true);
        headerField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        headerField.setBold(true);
        headerField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        // Dodanie wcięcia poprzez spacje: każdy poziom = 3 spacje
        headerField.setExpression(new JRDesignExpression(
            "\"   \".repeat(Math.max(0, $F{level} - 1)) + $F{text}"
        ));
        headerField.setPrintWhenExpression(new JRDesignExpression("$F{type}.equals(\"HEADER\")"));
        headerField.setFontSize(9f);

        // WAŻNE: Aby zakładka została utworzona, musi być anchor i bookmarkLevel
        headerField.setAnchorNameExpression(new JRDesignExpression(
            "\"bookmark_\" + $F{elementIndex}"
        ));
        headerField.setBookmarkLevelExpression(new JRDesignExpression("$F{level}"));

        headerField.setPositionType(PositionTypeEnum.FLOAT);
        detailBand.addElement(headerField);

        // Element 2: Para klucz-wartość
        JRDesignTextField keyValueField = new JRDesignTextField();
        keyValueField.setX(0);
        keyValueField.setY(0);
        keyValueField.setWidth(design.getColumnWidth());
        keyValueField.setHeight(9);
        keyValueField.setRemoveLineWhenBlank(true); // Usuń linię gdy pusty
        keyValueField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        keyValueField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        keyValueField.setFontSize(7f);
        keyValueField.setMarkup("html");
        // Dodanie wcięcia: poziom nagłówka + 1 dodatkowa spacja
        keyValueField.setExpression(new JRDesignExpression(
            "\"&nbsp;\".repeat(Math.max(0, $F{level} - 1) * 3 + 3) + \"<b>\" + $F{text} + \":</b> \" + $F{value}"
        ));
        keyValueField.setPrintWhenExpression(new JRDesignExpression("$F{type}.equals(\"KEY_VALUE\")"));
        keyValueField.setPositionType(PositionTypeEnum.FLOAT);
        detailBand.addElement(keyValueField);

        for (int i = 0; i < reportElements.size(); i++) {
            if ("TABLE".equals(reportElements.get(i).type)) {
                JRDesignSubreport subreport = new JRDesignSubreport(design);
                subreport.setX(0);
                subreport.setY(0);
                subreport.setWidth(design.getColumnWidth());
                subreport.setHeight(1);
                subreport.setRemoveLineWhenBlank(true);
                subreport.setPositionType(PositionTypeEnum.FLOAT);

                subreport.setExpression(new JRDesignExpression("$P{TABLE_REPORT_" + i + "}"));
                subreport.setDataSourceExpression(new JRDesignExpression("$P{TABLE_DATA_" + i + "}"));
                subreport.setPrintWhenExpression(new JRDesignExpression("$F{type}.equals(\"TABLE\") && $F{elementIndex}.equals(" + i + ")"));

                detailBand.addElement(subreport);
            }
        }

        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);

        JasperReport jasperReport = JasperCompileManager.compileReport(design);
        this.lastGeneratedDesign = design;

        if (printJrxmlToConsole) {
            printJrxmlToConsole(jasperReport, "MAIN DYNAMIC REPORT");
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, dataSource);

        // WAŻNE: Ustaw property create.bookmarks w JasperPrint aby zakładki były generowane
        jasperPrint.setProperty("net.sf.jasperreports.create.bookmarks", "true");

        return jasperPrint;
    }

    /**
     * Kompiluje subreport dla tabeli z danych JSON.
     */
    private JasperReport compileTableSubreport(JsonNode tableData, int availableWidth) throws JRException {
        if (!tableData.isArray() || tableData.isEmpty()) {
            throw new JRException("Table data must be a non-empty array");
        }

        JsonNode firstRow = tableData.get(0);
        if (!firstRow.isObject()) {
            throw new JRException("Table rows must be objects");
        }

        // Pobierz nazwy kolumn
        List<String> columnNames = new ArrayList<>();
        firstRow.fieldNames().forEachRemaining(columnNames::add);

        // Użyj ReportBuilder do stworzenia subreportu
        ReportBuilder tableBuilder = new ReportBuilder("TableSubreport")
                .withTheme(ReportTheme.DEFAULT)
                .withTitleBand(false)
                .withPageFooter(false)
                .withSummaryBand(false)
                .withColumnWidth(availableWidth);

        // Nadpisz styl nagłówka aby miał rozmiar czcionki 7
        Style tableHeaderStyle = new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 7, true)
                .withColors("#FFFFFF", "#2A3F54")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#1E2A38")
                .withPadding(1); // Zmniejszone z 2 na 1

        tableBuilder.addStyle(tableHeaderStyle);

        // Dodaj kolumny
        for (String columnName : columnNames) {
            tableBuilder.addColumn(new Column(
                    columnName,
                    columnName,
                    -1, // auto width
                    DataType.STRING,
                    null,
                    Calculation.NONE,
                    Calculation.NONE,
                    ReportStyles.DATA_STYLE
            ));
        }

        tableBuilder.calculateColumnWidths();
        return tableBuilder.build();
    }

    /**
     * Tworzy źródło danych dla tabeli z JSON.
     */
    private JRDataSource createTableDataSource(JsonNode tableData) {
        List<Map<String, ?>> rows = new ArrayList<>();

        if (tableData.isArray()) {
            for (JsonNode row : tableData) {
                if (row.isObject()) {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    row.fields().forEachRemaining(entry -> {
                        String key = entry.getKey();
                        JsonNode value = entry.getValue();
                        rowMap.put(key, value.isNull() ? "" : value.asText());
                    });
                    rows.add(rowMap);
                }
            }
        }

        return new JRMapCollectionDataSource(rows);
    }


    //TODO zrobić strone tytułową, automatyczne generowany spis treści , sekcje z nagłówkami, Plan Działania: Implementacja AutomatedReportFacade

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

        JRDesignParameter p1 = new JRDesignParameter();
        p1.setName(subreportParamName);
        p1.setValueClass(JasperReport.class);
        mainDesign.addParameter(p1);
        JRDesignParameter p2 = new JRDesignParameter();
        p2.setName(dataSourceParamName);
        p2.setValueClass(JRDataSource.class);
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

    private Object convertJsonValue(JsonNode value) {
        if (value == null || value.isNull()) return null;
        if (value.isTextual()) {
            try {
                return Date.from(Instant.parse(value.asText()));
            } catch (DateTimeParseException e) {
                return value.asText();
            }
        }
        if (value.isNumber()) return new BigDecimal(value.asText());
        if (value.isBoolean()) return value.asBoolean();
        return value.toString();
    }


    private Map<String, Object> flattenJson(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        addKeys("", node, map);
        return map;
    }

    List<ReportElement> flattenJsonForDebugging(JsonNode rootNode) {
        List<ReportElement> elements = new ArrayList<>();
        rootNode.fields().forEachRemaining(entry -> {
            flattenNodeRecursive(entry.getValue(), elements, 1, entry.getKey());
        });
        return elements;
    }

    private void flattenNodeRecursive(JsonNode node, List<ReportElement> elements, int level, String key) {
        if (node.isObject()) {
            ReportElement header = new ReportElement();
            header.type = "HEADER";
            header.text = key;
            header.level = level;
            elements.add(header);

            node.fields().forEachRemaining(entry -> {
                flattenNodeRecursive(entry.getValue(), elements, level + 1, entry.getKey());
            });

        } else if (node.isArray() && node.size() > 0 && node.get(0).isObject()) {
            ReportElement table = new ReportElement();
            table.type = "TABLE";
            table.text = key;
            table.level = level;
            table.rawTableData = node;
            elements.add(table);

        } else if (node.isValueNode()) {
            ReportElement kv = new ReportElement();
            kv.type = "KEY_VALUE";
            kv.text = key;
            kv.value = node.asText("null");
            kv.level = level;
            elements.add(kv);
        }
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

//    private Object convertJsonValue(JsonNode value) {
//        if (value == null || value.isNull()) return null;
//        if (value.isTextual()) {
//            try {
//                return Date.from(Instant.parse(value.asText()));
//            } catch (DateTimeParseException e) {
//                return value.asText();
//            }
//        }
//        if (value.isNumber()) return new BigDecimal(value.asText());
//        if (value.isBoolean()) return value.asBoolean();
//        return value.toString();
//    }

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

    private List<Map<String, Object>> convertJsonToArrayList(JsonNode arrayNode) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                if (item.isObject()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    item.fields().forEachRemaining(entry -> map.put(entry.getKey(), convertJsonValue(entry.getValue())));
                    result.add(map);
                }
            }
        }
        return result;
    }



    private static class ReportStructure {
        private final Set<String> fields = new LinkedHashSet<>();
        private final Map<String, DataType> fieldTypes = new HashMap<>();
        private final Map<String, ReportStructure> nestedStructures = new HashMap<>();

        public Set<String> getFields() {
            return fields;
        }

        public Map<String, DataType> getFieldTypes() {
            return fieldTypes;
        }

        public Map<String, ReportStructure> getNestedStructures() {
            return nestedStructures;
        }
    }

    static class ReportElement {
        String type;
        String text;
        String value;
        int level;
        JsonNode rawTableData;


        @Override
        public String toString() {
            return String.format("Typ: %-15s | Poziom: %d | Tekst: %s", type, level, text != null ? text : "ROOT");
        }
    }

}