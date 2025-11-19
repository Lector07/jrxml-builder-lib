package pl.lib.api;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import pl.lib.config.FormattingOptions;
import pl.lib.config.HighlightRule;
import pl.lib.config.ReportTheme;
import pl.lib.config.ThemeFactory;
import pl.lib.model.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.JRDataSource;
public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private final List<Style> styles = new ArrayList<>();
    private final JasperDesign jasperDesign;
    private final Map<String, Object> parameters = new HashMap<>();
    private final List<Group> groups = new ArrayList<>();
    private final List<Subreport> subreports = new ArrayList<>();
    private FormattingOptions formattingOptions = new FormattingOptions();
    private boolean pageFooterEnabled = true;
    private boolean titleEnabled = true;
    private boolean summaryBandEnabled = false;
    private String pageFormat = "A4";
    private ColorSettings colorSettings;
    public ReportBuilder() {
        this(UUID.randomUUID().toString());
    }
    public ReportBuilder(String reportName) {
        this.jasperDesign = new JasperDesign();
        this.jasperDesign.setName(reportName);
        this.jasperDesign.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
        this.jasperDesign.setLanguage("java");
    }
    public static void addBorder(JRDesignElement element, float lineWidth, java.awt.Color color) {
        if (element instanceof JRBoxContainer) {
            JRLineBox box = ((JRBoxContainer) element).getLineBox();
            box.getTopPen().setLineColor(color);
            box.getTopPen().setLineWidth(lineWidth);
            box.getBottomPen().setLineColor(color);
            box.getBottomPen().setLineWidth(lineWidth);
            box.getLeftPen().setLineColor(color);
            box.getLeftPen().setLineWidth(lineWidth);
            box.getRightPen().setLineColor(color);
            box.getRightPen().setLineWidth(lineWidth);
            box.setPadding(3);
        }
    }
    public ReportBuilder withPageFormat(String format) {
        if (format != null && !format.trim().isEmpty()) {
            this.pageFormat = format;
        }
        return this;
    }
    public ReportBuilder withColumnWidth(int width) {
        this.jasperDesign.setColumnWidth(width);
        return this;
    }
    public ReportBuilder withColorSettings(ColorSettings settings) {
        this.colorSettings = settings;
        return this;
    }
    public int preparePageAndGetColumnWidth() {
        setupPage();
        return this.jasperDesign.getColumnWidth();
    }
    public ReportBuilder withTitle(String title) {
        parameters.put("ReportTitle", title);
        return this;
    }
    public ReportBuilder withHorizontalLayout(boolean isLandscape) {
        if (isLandscape) {
            jasperDesign.setOrientation(OrientationEnum.LANDSCAPE);
        } else {
            jasperDesign.setOrientation(OrientationEnum.PORTRAIT);
        }
        return this;
    }
    public ReportBuilder withMargins(int top, int right, int bottom, int left) {
        this.jasperDesign.setTopMargin(top);
        this.jasperDesign.setRightMargin(right);
        this.jasperDesign.setBottomMargin(bottom);
        this.jasperDesign.setLeftMargin(left);
        return this;
    }
    public ReportBuilder withCompanyInfo(CompanyInfo companyInfo) {
        return this;
    }
    public ReportBuilder withTitleBand(boolean enabled) {
        this.titleEnabled = enabled;
        return this;
    }
    public ReportBuilder withPageFooter(boolean enabled) {
        this.pageFooterEnabled = enabled;
        return this;
    }
    public ReportBuilder withFormattingOptions(FormattingOptions options) {
        if (options != null) {
            this.formattingOptions = options;
        }
        return this;
    }
    public ReportBuilder withSummaryBand(boolean enabled) {
        this.summaryBandEnabled = enabled;
        return this;
    }
    public Map<String, Object> getParameters() {
        return this.parameters;
    }
    public ReportBuilder addColumn(Column column) {
        this.columns.add(column);
        return this;
    }
    public ReportBuilder addGroup(Group group) {
        this.groups.add(group);
        return this;
    }
    public ReportBuilder addStyle(Style style) {
        if (styles.stream().noneMatch(s -> s.getName().equals(style.getName()))) {
            this.styles.add(style);
        }
        return this;
    }
    public ReportBuilder addSubreport(Subreport subreport) {
        this.subreports.add(subreport);
        return this;
    }
    public JasperReport build() throws JRException {
        declareParameters();
        setupPage();
        calculateColumnWidths();
        buildStyles();
        declareFields();
        buildGroups();
        declareVariables();
        buildTitleBand();
        buildColumnHeaderBand();
        buildDetailBand();
        buildPageFooterBand();
        buildSummaryBand();
        return JasperCompileManager.compileReport(this.jasperDesign);
    }
    private void setupPage() {
        int width;
        int height;
        switch (this.pageFormat.toUpperCase()) {
            case "A3":
                width = 842;
                height = 1190;
                break;
            case "LETTER":
                width = 612;
                height = 792;
                break;
            case "LEGAL":
                width = 612;
                height = 1008;
                break;
            case "A4":
            default:
                width = 595;
                height = 842;
                break;
        }
        if (jasperDesign.getOrientationValue() == OrientationEnum.LANDSCAPE) {
            int temp = width;
            width = height;
            height = temp;
        }
        jasperDesign.setPageWidth(width);
        jasperDesign.setPageHeight(height);
        int columnWidth = jasperDesign.getPageWidth() - jasperDesign.getLeftMargin() - jasperDesign.getRightMargin();
        jasperDesign.setColumnWidth(columnWidth);
    }
    private void declareFields() throws JRException {
        Map<String, Class<?>> fieldTypeMap = new HashMap<>();
        for (Column column : columns) {
            String fieldName = column.getFieldName();
            String jrFieldName = fieldName.replace('.', '_');
            fieldTypeMap.put(fieldName, column.getType().getJavaClass());
            if (jasperDesign.getFieldsMap().get(jrFieldName) == null) {
                JRDesignField field = new JRDesignField();
                field.setName(jrFieldName);
                field.setValueClass(column.getType().getJavaClass());
                jasperDesign.addField(field);
            }
        }
        for (Group group : groups) {
            String fieldName = group.getFieldName();
            String jrFieldName = fieldName.replace('.', '_');
            if (jasperDesign.getFieldsMap().get(jrFieldName) == null) {
                JRDesignField field = new JRDesignField();
                field.setName(jrFieldName);
                Class<?> fieldClass = fieldTypeMap.getOrDefault(fieldName, String.class);
                field.setValueClass(fieldClass);
                jasperDesign.addField(field);
            }
        }
        for (Subreport subreport : subreports) {
            String jrFieldName = subreport.getFieldName().replace('.', '_');
            if (jasperDesign.getFieldsMap().get(jrFieldName) == null) {
                JRDesignField field = new JRDesignField();
                field.setName(jrFieldName);
                field.setValueClass(JRDataSource.class);
                jasperDesign.addField(field);
            }
        }
    }
    private void declareParameters() throws JRException {
        addParameterIfNotExists("ReportTitle", String.class);
        addParameterIfNotExists("CompanyName", String.class);
        addParameterIfNotExists("CompanyAddress", String.class);
        addParameterIfNotExists("CompanyPostalCode", String.class);
        addParameterIfNotExists("CompanyCity", String.class);
        addParameterIfNotExists("FooterLeftText", String.class);
        addParameterIfNotExists("CompanyTaxId", String.class);
        addParameterIfNotExists("SHOW_SUMMARY", Boolean.class);
        for (Subreport sub : subreports) {
            addParameterIfNotExists("SUBREPORT_" + sub.getFieldName(), JasperReport.class);
        }
    }
    private void addParameterIfNotExists(String name, Class<?> type) throws JRException {
        if (jasperDesign.getParametersMap().get(name) == null) {
            JRDesignParameter param = new JRDesignParameter();
            param.setName(name);
            param.setValueClass(type);
            jasperDesign.addParameter(param);
        }
    }
    private void declareVariables() throws JRException {
        for (Column column : columns) {
            if (column.hasGroupCalculation()) {
                for (Group group : this.groups) {
                    String jrGroupFieldName = group.getFieldName().replace('.', '_');
                    String groupName = "Group_" + jrGroupFieldName;
                    JRGroup jrGroup = jasperDesign.getGroupsMap().get(groupName);
                    if (jrGroup != null) {
                        String jrFieldName = column.getFieldName().replace('.', '_');
                        String variableName = jrFieldName + "_" + groupName + "_SUM";
                        if (jasperDesign.getVariablesMap().get(variableName) == null) {
                            JRDesignVariable variable = new JRDesignVariable();
                            variable.setName(variableName);
                            variable.setValueClass(column.getType().getJavaClass());
                            variable.setResetType(ResetTypeEnum.GROUP);
                            variable.setResetGroup(jrGroup);
                            variable.setCalculation(toJasperCalculation(column.getGroupCalculation()));
                            variable.setExpression(new JRDesignExpression("$F{" + jrFieldName + "}"));
                            jasperDesign.addVariable(variable);
                        }
                    }
                }
            }
        }
    }
    private CalculationEnum toJasperCalculation(Calculation calc) {
        if (calc == null) return CalculationEnum.NOTHING;
        switch (calc) {
            case SUM:
                return CalculationEnum.SUM;
            case COUNT:
                return CalculationEnum.COUNT;
            case AVERAGE:
                return CalculationEnum.AVERAGE;
            default:
                return CalculationEnum.NOTHING;
        }
    }
    private void buildTitleBand() {
        if (!titleEnabled) return;
        int availableWidth = jasperDesign.getColumnWidth();
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(95);
        titleBand.addElement(createTextField("$P{CompanyName}", 0, 0, availableWidth / 2, 18, true, 8f));
        titleBand.addElement(createTextField("$P{CompanyAddress}", 0, 18, availableWidth / 2, 15, false, 8f));
        titleBand.addElement(createTextField("$P{CompanyPostalCode} + \" \" + $P{CompanyCity}", 0, 33, availableWidth / 2, 15, false, 8f));
        titleBand.addElement(createTextField("$P{CompanyTaxId} != null ? \"NIP: \" + $P{CompanyTaxId} : \"\"", 0, 48, availableWidth / 2, 15, false, 8f));
        JRDesignTextField dateField = createTextField("\"Data: \" + new java.text.SimpleDateFormat(\"dd.MM.yyyy\").format(new java.util.Date())", 0, 18, availableWidth, 15, false, 8f);
        dateField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        titleBand.addElement(dateField);
        JRDesignTextField titleTextField = createTextField("$P{ReportTitle}", 0, 65, availableWidth, 25, true, 10f);
        String titleBg = (colorSettings != null && colorSettings.getTitleBackgroundColor() != null)
                ? colorSettings.getTitleBackgroundColor()
                : "#2A3F54";
        String titleFg = (colorSettings != null && colorSettings.getTitleFontColor() != null)
                ? colorSettings.getTitleFontColor()
                : "#FFFFFF";
        titleTextField.setForecolor(Color.decode(titleFg));
        titleTextField.setBackcolor(Color.decode(titleBg));
        titleTextField.setMode(ModeEnum.OPAQUE);
        titleTextField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleTextField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleBand.addElement(titleTextField);
        jasperDesign.setTitle(titleBand);
    }
    private JRDesignTextField createTextField(String expressionText, int x, int y, int w, int h, boolean isBold, float fontSize) {
        JRDesignTextField textField = new JRDesignTextField();
        textField.setX(x);
        textField.setY(y);
        textField.setWidth(w);
        textField.setHeight(h);
        textField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        textField.setBold(isBold);
        textField.setFontSize(fontSize);
        textField.setExpression(new JRDesignExpression(expressionText));
        return textField;
    }
    private void buildColumnHeaderBand() {
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(15);
        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() <= 0) continue;
            JRDesignStaticText headerText = new JRDesignStaticText();
            headerText.setX(currentX);
            headerText.setY(0);
            headerText.setWidth(column.getWidth());
            headerText.setHeight(15);
            headerText.setText(column.getTitle());
            headerText.setStyle(jasperDesign.getStylesMap().get(ReportStyles.HEADER_STYLE));
            columnHeaderBand.addElement(headerText);
            currentX += column.getWidth();
        }
        jasperDesign.setColumnHeader(columnHeaderBand);
    }
    private void buildDetailBand() throws JRException {
        JRDesignSection detailSection = (JRDesignSection) jasperDesign.getDetailSection();
        if (columns.stream().anyMatch(c -> c.getWidth() > 0)) {
            JRDesignBand dataBand = new JRDesignBand();
            dataBand.setHeight(12);
            int currentX = 0;
            boolean bookmarkAssigned = false;
            String desiredBookmarkField = (formattingOptions != null) ? formattingOptions.getBookmarkField() : null;
            for (Column column : columns) {
                if (column.getWidth() <= 0) continue;
                String jrFieldName = column.getFieldName().replace('.', '_');
                JRDesignTextField dataField = createTextField("$F{" + jrFieldName + "}", currentX, 0, column.getWidth(), 12, false, 7f);
                dataField.setStyle(jasperDesign.getStylesMap().get(column.getStyleName()));
                dataField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
                dataField.setBlankWhenNull(true);
                if (column.hasPattern()) dataField.setPattern(column.getPattern());
                dataField.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
                if (formattingOptions != null && formattingOptions.isGenerateBookmarks() && (groups == null || groups.isEmpty())) {
                    boolean isDesired = desiredBookmarkField != null && desiredBookmarkField.equals(column.getFieldName());
                    if ((isDesired || (!bookmarkAssigned && desiredBookmarkField == null)) && !bookmarkAssigned) {
                        dataField.setBookmarkLevel(1);
                        bookmarkAssigned = true;
                    }
                }
                dataBand.addElement(dataField);
                currentX += column.getWidth();
            }
            detailSection.addBand(dataBand);
        }
        if (!subreports.isEmpty()) {
            for (Subreport sub : subreports) {
                JRDesignBand subreportBand = new JRDesignBand();
                subreportBand.setHeight(1);
                subreportBand.setSplitType(SplitTypeEnum.STRETCH);
                JRDesignSubreport jrSubreport = new JRDesignSubreport(jasperDesign);
                jrSubreport.setX(-jasperDesign.getRightMargin());
                jrSubreport.setY(0);
                jrSubreport.setWidth(jasperDesign.getColumnWidth());
                jrSubreport.setHeight(1);
                jrSubreport.setPositionType(PositionTypeEnum.FLOAT);
                jrSubreport.setRemoveLineWhenBlank(true);
                jrSubreport.setUsingCache(true);
                jrSubreport.setExpression(new JRDesignExpression("$P{SUBREPORT_" + sub.getFieldName() + "}"));
                jrSubreport.setDataSourceExpression(new JRDesignExpression("$F{" + sub.getFieldName().replace('.', '_') + "}"));
                JRDesignSubreportParameter showSummaryParam = new JRDesignSubreportParameter();
                showSummaryParam.setName("SHOW_SUMMARY");
                showSummaryParam.setExpression(new JRDesignExpression(sub.isShowSummary() ? "java.lang.Boolean.TRUE" : "java.lang.Boolean.FALSE"));
                jrSubreport.addParameter(showSummaryParam);
                subreportBand.addElement(jrSubreport);
                detailSection.addBand(subreportBand);
            }
        }
    }
    public void calculateColumnWidths() {
        int availableWidth = jasperDesign.getColumnWidth();
        List<Column> visibleColumns = columns.stream().filter(c -> c.getWidth() != 0).collect(Collectors.toList());
        int fixedWidthTotal = visibleColumns.stream().filter(c -> c.getWidth() > 0).mapToInt(Column::getWidth).sum();
        long autoWidthColumnsCount = visibleColumns.stream().filter(c -> c.getWidth() < 0).count();
        if (autoWidthColumnsCount > 0) {
            int autoWidth = (availableWidth - fixedWidthTotal) / (int) autoWidthColumnsCount;
            List<Column> updatedColumns = new ArrayList<>();
            for (Column c : this.columns) {
                if (c.getWidth() < 0) {
                    updatedColumns.add(c.withWidth(autoWidth));
                } else {
                    updatedColumns.add(c);
                }
            }
            this.columns.clear();
            this.columns.addAll(updatedColumns);
        }
    }
    private void buildGroups() throws JRException {
        int totalColumnWidth = columns.stream()
                .filter(c -> c.getWidth() > 0)
                .mapToInt(Column::getWidth)
                .sum();
        int indentationStep = 20;
        for (int i = 0; i < this.groups.size(); i++) {
            Group group = this.groups.get(i);
            String jrFieldName = group.getFieldName().replace('.', '_');
            String groupName = "Group_" + jrFieldName;
            if (jasperDesign.getGroupsMap().get(groupName) != null) continue;
            JRDesignGroup jrGroup = new JRDesignGroup();
            jrGroup.setName(groupName);
            jrGroup.setExpression(new JRDesignExpression("$F{" + jrFieldName + "}"));
            if (group.isShowGroupHeader() || group.isShowGroupFooter()) {
                JRDesignBand groupHeaderBand = new JRDesignBand();
                groupHeaderBand.setHeight(20);
                int indentation = i * indentationStep;
                boolean showSummaryInHeader = group.isShowGroupFooter();
                JRDesignTextField groupHeaderField;
                if (showSummaryInHeader) {
                    int bgWidth = Math.max(0, totalColumnWidth - indentation);
                    JRDesignStaticText background = new JRDesignStaticText();
                    background.setX(indentation);
                    background.setY(0);
                    background.setWidth(jasperDesign.getColumnWidth() - indentation);
                    background.setHeight(20);
                    background.setStyle(jasperDesign.getStylesMap().get(group.getStyleName()));
                    groupHeaderBand.addElement(background);
                    int firstSumColumnX = totalColumnWidth;
                    int currentX = 0;
                    for (Column column : columns) {
                        if (column.getWidth() > 0 && column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                            firstSumColumnX = currentX;
                            break;
                        }
                        if (column.getWidth() > 0) currentX += column.getWidth();
                    }
                    int labelWidth = firstSumColumnX - indentation;
                    if (labelWidth > 0) {
                        groupHeaderField = createTextField(group.getHeaderExpression(), indentation, 0, labelWidth, 20, true, 7f);
                        groupHeaderField.setStyle(getTransparentStyle(group.getStyleName()));
                        groupHeaderBand.addElement(groupHeaderField);
                    } else {
                        groupHeaderField = createTextField(group.getHeaderExpression(), indentation, 0, 0, 0, true, 0f);
                    }
                    currentX = 0;
                    for (Column column : columns) {
                        if (column.getWidth() <= 0) continue;
                        if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                            String variableName = column.getFieldName().replace('.', '_') + "_" + groupName + "_SUM";
                            JRDesignTextField sumField = createTextField("$V{" + variableName + "}", currentX, 0, column.getWidth(), 20, true, 7f);
                            sumField.setStyle(getTransparentStyle(group.getStyleName()));
                            sumField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
                            if (column.hasPattern()) sumField.setPattern(column.getPattern());
                            sumField.setEvaluationTime(EvaluationTimeEnum.GROUP);
                            sumField.setEvaluationGroup(jrGroup);
                            groupHeaderBand.addElement(sumField);
                        }
                        currentX += column.getWidth();
                    }
                } else {
                    int availableWidth = Math.max(0, totalColumnWidth - indentation);
                    groupHeaderField = createTextField(group.getHeaderExpression(), indentation, 0, availableWidth, 20, false, 7f);
                    groupHeaderField.setStyle(jasperDesign.getStylesMap().get(group.getStyleName()));
                    groupHeaderBand.addElement(groupHeaderField);
                }
                if (formattingOptions != null && formattingOptions.isGenerateBookmarks()) {
                    groupHeaderField.setBookmarkLevel(i + 1);
                }
                ((JRDesignSection) jrGroup.getGroupHeaderSection()).addBand(groupHeaderBand);
            }
            jasperDesign.addGroup(jrGroup);
        }
    }
    private void buildPageFooterBand() throws JRException {
        if (!pageFooterEnabled) return;
        JRDesignBand pageFooterBand = new JRDesignBand();
        pageFooterBand.setHeight(35);
        JRDesignTextField leftText = createTextField("$P{FooterLeftText}", 0, 2, jasperDesign.getColumnWidth() / 2, 30, false, 8f);
        leftText.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        leftText.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        leftText.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        pageFooterBand.addElement(leftText);
        JRDesignTextField pageNumberField = new JRDesignTextField();
        pageNumberField.setX(0);
        pageNumberField.setY(12);
        pageNumberField.setWidth(jasperDesign.getColumnWidth());
        pageNumberField.setHeight(20);
        pageNumberField.setExpression(new JRDesignExpression("\"Strona \" + $V{PAGE_NUMBER}"));
        pageNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pageNumberField.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        pageNumberField.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        pageNumberField.setFontSize(8f);
        pageFooterBand.addElement(pageNumberField);
        jasperDesign.setPageFooter(pageFooterBand);
    }
    private void buildSummaryBand() throws JRException {
        if (!summaryBandEnabled) return;
        boolean hasCalculations = columns.stream().anyMatch(column ->
                column.hasGroupCalculation() ||
                        (column.getDataType() != null && column.getDataType().isNumeric())
        );
        int summaryHeight = hasCalculations ? 25 : 20;
        JRDesignBand summaryBand = new JRDesignBand();
        summaryBand.setHeight(summaryHeight);
        summaryBand.setPrintWhenExpression(
                new JRDesignExpression("$P{SHOW_SUMMARY} == null ? java.lang.Boolean.TRUE : $P{SHOW_SUMMARY}")
        );
        if (hasCalculations) {
            declareReportSummaryVariables();
            int summaryY = 0;
            JRDesignRectangle backgroundRect = new JRDesignRectangle();
            backgroundRect.setX(0);
            backgroundRect.setY(summaryY);
            backgroundRect.setWidth(jasperDesign.getColumnWidth());
            backgroundRect.setHeight(20);
            backgroundRect.setBackcolor(Color.decode("#F0F0F0"));
            backgroundRect.setMode(ModeEnum.OPAQUE);
            backgroundRect.setFill(FillEnum.SOLID);
            backgroundRect.getLinePen().setLineWidth(0.5f);
            backgroundRect.getLinePen().setLineColor(Color.decode("#D6D6D6"));
            summaryBand.addElement(backgroundRect);
            int firstNumericColumnX = 0;
            int currentX = 0;
            boolean foundFirstNumeric = false;
            for (Column column : columns) {
                if (column.getWidth() <= 0) continue;
                boolean isNumericColumn = column.getDataType() != null &&
                        (column.getDataType().isNumeric() || column.hasGroupCalculation());
                if (isNumericColumn && !foundFirstNumeric) {
                    firstNumericColumnX = currentX;
                    foundFirstNumeric = true;
                    break;
                }
                currentX += column.getWidth();
            }
            currentX = 0;
            for (Column column : columns) {
                if (column.getWidth() <= 0) {
                    continue;
                }
                boolean shouldShowSummary = column.getDataType() != null &&
                        (column.getDataType().isNumeric() || column.hasGroupCalculation());
                if (shouldShowSummary) {
                    String jrFieldName = column.getFieldName().replace('.', '_');
                    String variableName = jrFieldName + "_REPORT_SUM";
                    JRDesignTextField summaryField = createTextField("$V{" + variableName + "}",
                            currentX, summaryY, column.getWidth(), 20, true, 8f);
                    summaryField.setMode(ModeEnum.TRANSPARENT);
                    summaryField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
                    summaryField.setForecolor(Color.decode("#000000"));
                    if (column.hasPattern()) {
                        summaryField.setPattern(column.getPattern());
                    }
                    summaryField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
                    summaryField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
                    summaryField.setEvaluationTime(EvaluationTimeEnum.REPORT);
                    summaryField.setBlankWhenNull(true);
                    summaryBand.addElement(summaryField);
                }
                currentX += column.getWidth();
            }
        }
        jasperDesign.setSummary(summaryBand);
    }
    private void declareReportSummaryVariables() throws JRException {
        for (Column column : columns) {
            if (column.getDataType() != null && column.getDataType().isNumeric()) {
                String jrFieldName = column.getFieldName().replace('.', '_');
                String variableName = jrFieldName + "_REPORT_SUM";
                if (jasperDesign.getVariablesMap().get(variableName) == null) {
                    JRDesignVariable variable = new JRDesignVariable();
                    variable.setName(variableName);
                    variable.setValueClass(column.getType().getJavaClass());
                    variable.setResetType(ResetTypeEnum.REPORT);
                    variable.setCalculation(CalculationEnum.SUM);
                    variable.setExpression(new JRDesignExpression("$F{" + jrFieldName + "}"));
                    jasperDesign.addVariable(variable);
                }
            }
        }
    }
    private void buildStyles() throws JRException {
        for (Style style : this.styles) {
            if (jasperDesign.getStylesMap().get(style.getName()) == null) {
                JRDesignStyle jrStyle = new JRDesignStyle();
                jrStyle.setName(style.getName());
                jrStyle.setFontName(style.getFontName());
                jrStyle.setFontSize(style.getFontSize());
                jrStyle.setBold(style.isBold());
                if (style.getFontColor() != null) jrStyle.setForecolor(Color.decode(style.getFontColor()));
                if (style.getBackColor() != null) {
                    jrStyle.setBackcolor(Color.decode(style.getBackColor()));
                    jrStyle.setMode(ModeEnum.OPAQUE);
                }
                jrStyle.setHorizontalTextAlign(HorizontalTextAlignEnum.valueOf(style.getHorizontalAlignment().toUpperCase()));
                jrStyle.setVerticalTextAlign(VerticalTextAlignEnum.valueOf(style.getVerticalAlignment().toUpperCase()));
                if (style.getBorderWidth() > 0)
                    setBox(jrStyle.getLineBox(), Color.decode(style.getBorderColor()), style.getBorderWidth());
                if (style.getPadding() != null) jrStyle.getLineBox().setPadding(style.getPadding());
                jasperDesign.addStyle(jrStyle);
            }
        }
        JRDesignStyle dataStyle = (JRDesignStyle) jasperDesign.getStylesMap().get(ReportStyles.DATA_STYLE);
        JRDesignStyle numericDataStyle = (JRDesignStyle) jasperDesign.getStylesMap().get(ReportStyles.NUMERIC_STYLE);
        if (dataStyle == null || formattingOptions == null) return;
        if (formattingOptions.getHighlightRules() != null) {
            for (HighlightRule rule : formattingOptions.getHighlightRules()) {
                JRDesignConditionalStyle highlightStyleData = new JRDesignConditionalStyle();
                try {
                    highlightStyleData.setBackcolor(Color.decode(rule.getColor()));
                    highlightStyleData.setMode(ModeEnum.OPAQUE);
                } catch (Exception e) {
                    System.err.println("Invalid color format: " + rule.getColor() + ". Using YELLOW.");
                    highlightStyleData.setBackcolor(Color.YELLOW);
                    highlightStyleData.setMode(ModeEnum.OPAQUE);
                }
                String conditionText = buildConditionExpression(rule);
                if (conditionText != null) {
                    highlightStyleData.setConditionExpression(new JRDesignExpression(conditionText));
                    dataStyle.addConditionalStyle(highlightStyleData);
                }
                if (numericDataStyle != null) {
                    JRDesignConditionalStyle highlightStyleNumeric = new JRDesignConditionalStyle();
                    try {
                        highlightStyleNumeric.setBackcolor(Color.decode(rule.getColor()));
                        highlightStyleNumeric.setMode(ModeEnum.OPAQUE);
                    } catch (Exception e) {
                        highlightStyleNumeric.setBackcolor(Color.YELLOW);
                        highlightStyleNumeric.setMode(ModeEnum.OPAQUE);
                    }
                    if (conditionText != null) {
                        highlightStyleNumeric.setConditionExpression(new JRDesignExpression(conditionText));
                        numericDataStyle.addConditionalStyle(highlightStyleNumeric);
                    }
                }
            }
        }
        if (formattingOptions.isZebraStripes()) {
            JRDesignConditionalStyle zebraStyleData = new JRDesignConditionalStyle();
            zebraStyleData.setBackcolor(Color.decode("#F7F7F7"));
            zebraStyleData.setMode(ModeEnum.OPAQUE);
            zebraStyleData.setConditionExpression(new JRDesignExpression("$V{REPORT_COUNT} % 2 == 0"));
            dataStyle.addConditionalStyle(zebraStyleData);
            if (numericDataStyle != null) {
                JRDesignConditionalStyle zebraStyleNumeric = new JRDesignConditionalStyle();
                zebraStyleNumeric.setBackcolor(Color.decode("#F7F7F7"));
                zebraStyleNumeric.setMode(ModeEnum.OPAQUE);
                zebraStyleNumeric.setConditionExpression(new JRDesignExpression("$V{REPORT_COUNT} % 2 == 0"));
                numericDataStyle.addConditionalStyle(zebraStyleNumeric);
            }
        }
    }
    private String buildConditionExpression(HighlightRule rule) {
        String fieldName = "$F{" + rule.getField().replace('.', '_') + "}";
        DataType fieldType = columns.stream()
                .filter(c -> c.getFieldName().equals(rule.getField()))
                .map(Column::getDataType)
                .findFirst()
                .orElse(DataType.STRING);
        String value = rule.getValue();
        String formattedValue;
        if (fieldType.isNumeric()) {
            try {
                new java.math.BigDecimal(value);
                formattedValue = "new java.math.BigDecimal(\"" + value.replace("\"", "\\\"") + "\")";
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for rule value: " + value);
                return "Boolean.FALSE";
            }
        } else {
            formattedValue = "\"" + value.replace("\"", "\\\"") + "\"";
        }
        String fieldForComparison = fieldType.isNumeric() ? fieldName : fieldName + ".toString()";
        switch (rule.getOperator()) {
            case "EQUALS":
                return fieldName + " != null && " + fieldForComparison + ".equals(" + formattedValue + ")";
            case "NOT_EQUALS":
                return fieldName + " != null && !" + fieldForComparison + ".equals(" + formattedValue + ")";
            case "CONTAINS":
                return fieldType == DataType.STRING ? fieldName + " != null && " + fieldName + ".contains(" + formattedValue + ")" : "Boolean.FALSE";
            case "GREATER_THAN":
                return fieldType.isNumeric() ? fieldName + " != null && " + fieldName + ".compareTo(" + formattedValue + ") > 0" : "Boolean.FALSE";
            case "LESS_THAN":
                return fieldType.isNumeric() ? fieldName + " != null && " + fieldName + ".compareTo(" + formattedValue + ") < 0" : "Boolean.FALSE";
            default:
                return "Boolean.FALSE";
        }
    }
    private void setBox(JRLineBox box, Color color, float width) {
        box.getTopPen().setLineColor(color);
        box.getTopPen().setLineWidth(width);
        box.getLeftPen().setLineColor(color);
        box.getLeftPen().setLineWidth(width);
        box.getBottomPen().setLineColor(color);
        box.getBottomPen().setLineWidth(width);
        box.getRightPen().setLineColor(color);
        box.getRightPen().setLineWidth(width);
    }
    private JRStyle getTransparentStyle(String baseStyleName) throws JRException {
        String transparentStyleName = baseStyleName + "_Transparent";
        JRStyle transparentStyle = jasperDesign.getStylesMap().get(transparentStyleName);
        if (transparentStyle == null) {
            JRStyle baseStyle = jasperDesign.getStylesMap().get(baseStyleName);
            if (baseStyle == null) return null;
            JRDesignStyle newStyle = (JRDesignStyle) baseStyle.clone();
            newStyle.setName(transparentStyleName);
            newStyle.setMode(ModeEnum.TRANSPARENT);
            newStyle.setBackcolor(null);
            jasperDesign.addStyle(newStyle);
            return newStyle;
        }
        return transparentStyle;
    }
    public ReportBuilder withTheme(ReportTheme theme) {
        if (theme != null) {
            this.styles.clear();
            List<Style> themeStyles = ThemeFactory.createStylesForTheme(theme);
            this.styles.addAll(themeStyles);
        }
        applyCustomColors();
        return this;
    }
    private void applyCustomColors() {
        if (colorSettings == null) {
            return;
        }
        modifyStyle(ReportStyles.HEADER_STYLE, style -> style
                .withColors(colorSettings.getColumnHeaderFontColor(), colorSettings.getColumnHeaderBackgroundColor())
                .withBorders(style.getBorderWidth(), colorSettings.getBorderColor())
        );
        modifyStyle(ReportStyles.DATA_STYLE, style -> style
                .withColors(colorSettings.getDetailFontColor(), colorSettings.getDetailBackgroundColor())
                .withBorders(style.getBorderWidth(), colorSettings.getBorderColor())
        );
        modifyStyle(ReportStyles.NUMERIC_STYLE, style -> style
                .withColors(colorSettings.getDetailFontColor(), colorSettings.getDetailBackgroundColor())
                .withBorders(style.getBorderWidth(), colorSettings.getBorderColor())
        );
        modifyStyle(ReportStyles.GROUP_STYLE_1, style -> style
                .withColors(colorSettings.getGroupHeaderFontColor(), colorSettings.getGroupHeaderBackgroundColor())
        );
        modifyStyle(ReportStyles.GROUP_STYLE_2, style -> style
                .withColors(colorSettings.getGroupHeaderFontColor(), colorSettings.getGroupHeaderBackgroundColor())
        );
    }
    private void modifyStyle(String styleName, java.util.function.Function<Style, Style> modifier) {
        Style originalStyle = styles.stream()
                .filter(s -> s.getName().equals(styleName))
                .findFirst()
                .orElse(null);
        if (originalStyle != null) {
            Style newStyle = modifier.apply(originalStyle);
            styles.remove(originalStyle);
            styles.add(newStyle);
        }
    }
    public JasperDesign getDesign() {
        return this.jasperDesign;
    }
}
