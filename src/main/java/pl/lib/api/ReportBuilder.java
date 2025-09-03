package pl.lib.api;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import pl.lib.model.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OSTATECZNA, KOMPLETNA I W PEŁNI POPRAWNA WERSJA ReportBuilder.
 * Zawiera poprawki wcięć, wyrównania ORAZ synchronizacji wysokości wierszy.
 */
public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private final List<Style> styles = new ArrayList<>();
    private final JasperDesign jasperDesign;
    private final Map<String, Object> parameters = new HashMap<>();
    private final List<Group> groups = new ArrayList<>();
    private boolean isForSubreport = false;
    private boolean pageFooterEnabled = false;

    public ReportBuilder(String reportName) {
        this.jasperDesign = new JasperDesign();
        this.jasperDesign.setName(reportName);
        this.jasperDesign.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
        this.jasperDesign.setLanguage("java");
    }

    public ReportBuilder() {
        this(UUID.randomUUID().toString());
    }

    public ReportBuilder withTitle(String title) {
        parameters.put("ReportTitle", title);
        return this;
    }

    public ReportBuilder withHorizontalLayout() {
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
        if (companyInfo != null) {
            parameters.put("CompanyName", companyInfo.getName());
            parameters.put("CompanyAddress", companyInfo.getAddress());
            parameters.put("CompanyPostalCode", companyInfo.getPostalCode());
            parameters.put("CompanyCity", companyInfo.getCity());
        }
        return this;
    }

    public ReportBuilder withPageFooter(boolean enabled) {
        this.pageFooterEnabled = enabled;
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

    public JasperReport build() throws JRException {
        setupPage();
        calculateColumnWidths();
        buildStyles();
        declareParameters();
        declareFields();
        buildGroups(); // Musi być przed declareVariables
        declareVariables();
        buildTitleBand();
        buildColumnHeaderBand();
        buildDetailBand();
        buildPageFooterBand();
        buildSummaryBand();
        return JasperCompileManager.compileReport(this.jasperDesign);
    }

    private void setupPage() {
        jasperDesign.setColumnWidth(jasperDesign.getPageWidth() - jasperDesign.getLeftMargin() - jasperDesign.getRightMargin());
    }

    private void declareFields() throws JRException {
        for (Column column : columns) {
            String fieldName = column.getFieldName();
            String jrFieldName = fieldName.replace('.', '_');
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
                field.setValueClass(String.class);
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
            case SUM: return CalculationEnum.SUM;
            case COUNT: return CalculationEnum.COUNT;
            case AVERAGE: return CalculationEnum.AVERAGE;
            default: return CalculationEnum.NOTHING;
        }
    }

    private void buildTitleBand() {
        int availableWidth = jasperDesign.getColumnWidth();
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(80);
        titleBand.addElement(createTextField("$P{CompanyName}", 0, 0, availableWidth / 2, 18, true, 8f));
        titleBand.addElement(createTextField("$P{CompanyAddress}", 0, 18, availableWidth / 2, 15, false, 8f));
        titleBand.addElement(createTextField("$P{CompanyPostalCode} + \" \" + $P{CompanyCity}", 0, 33, availableWidth / 2, 15, false, 8f));
        JRDesignTextField dateField = createTextField("\"Data: \" + new java.text.SimpleDateFormat(\"dd.MM.yyyy\").format(new java.util.Date())", availableWidth / 2, 18, availableWidth / 2, 15, false, 8f);
        dateField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        titleBand.addElement(dateField);
        JRDesignTextField titleTextField = createTextField("$P{ReportTitle}", 0, 50, availableWidth, 25, true, 10f);
        titleTextField.setForecolor(Color.decode("#FFFFFF"));
        titleTextField.setBackcolor(Color.decode("#2A3F54"));
        titleTextField.setMode(ModeEnum.OPAQUE);
        titleTextField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleTextField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleBand.addElement(titleTextField);
        jasperDesign.setTitle(titleBand);
    }

    private JRDesignTextField createTextField(String expressionText, int x, int y, int w, int h, boolean isBold, float fontSize) {
        JRDesignTextField textField = new JRDesignTextField();
        textField.setX(x); textField.setY(y);
        textField.setWidth(w); textField.setHeight(h);
        textField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        textField.setBold(isBold);
        textField.setFontSize(fontSize);
        textField.setExpression(new JRDesignExpression(expressionText));
        return textField;
    }

    private void buildColumnHeaderBand() {
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(20);
        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() <= 0) continue;
            JRDesignStaticText headerText = new JRDesignStaticText();
            headerText.setX(currentX); headerText.setY(0);
            headerText.setWidth(column.getWidth()); headerText.setHeight(20);
            headerText.setText(column.getTitle());
            headerText.setStyle((JRStyle) jasperDesign.getStylesMap().get(ReportStyles.HEADER_STYLE));
            columnHeaderBand.addElement(headerText);
            currentX += column.getWidth();
        }
        jasperDesign.setColumnHeader(columnHeaderBand);
    }

    private void buildDetailBand() throws JRException {
        JRDesignSection detailSection = (JRDesignSection) jasperDesign.getDetailSection();
        if (columns.stream().anyMatch(c -> c.getWidth() > 0)) {
            JRDesignBand dataBand = new JRDesignBand();
            dataBand.setHeight(20); // Wysokość domyślna, będzie się rozciągać
            int currentX = 0;
            for (Column column : columns) {
                if (column.getWidth() <= 0) continue;
                String jrFieldName = column.getFieldName().replace('.', '_');
                JRDesignTextField dataField = createTextField("$F{" + jrFieldName + "}", currentX, 0, column.getWidth(), 20, false, 7f);
                dataField.setStyle((JRStyle) jasperDesign.getStylesMap().get(column.getStyleName()));
                dataField.setStretchWithOverflow(true);
                dataField.setBlankWhenNull(true);
                if (column.hasPattern()) dataField.setPattern(column.getPattern());

                // =================================================================================
                // === OSTATECZNA POPRAWKA: SYNCHRONIZACJA WYSOKOŚCI WIERSZA ===
                // Ta linia mówi każdej komórce, aby dostosowała swoją wysokość do najwyższej komórki w wierszu.
                dataField.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
                // =================================================================================

                dataBand.addElement(dataField);
                currentX += column.getWidth();
            }
            detailSection.addBand(dataBand);
        }
    }

    private void calculateColumnWidths() {
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

                if (showSummaryInHeader) {
                    JRDesignStaticText background = new JRDesignStaticText();
                    background.setX(0); background.setY(0);
                    background.setWidth(jasperDesign.getColumnWidth()); background.setHeight(20);
                    background.setStyle((JRStyle) jasperDesign.getStylesMap().get(group.getStyleName()));
                    groupHeaderBand.addElement(background);
                    int firstSumColumnX = jasperDesign.getColumnWidth();
                    int currentX = 0;
                    for (Column column : columns) {
                        if (column.getWidth() > 0 && column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                            firstSumColumnX = currentX;
                            break;
                        }
                        if(column.getWidth() > 0) currentX += column.getWidth();
                    }
                    int labelWidth = firstSumColumnX - indentation;
                    if (labelWidth > 0) {
                        JRDesignTextField groupHeaderField = createTextField(group.getHeaderExpression(), indentation, 0, labelWidth, 20, true, 7f);
                        groupHeaderField.setStyle(getTransparentStyle(group.getStyleName()));
                        groupHeaderBand.addElement(groupHeaderField);
                    }
                    currentX = 0;
                    for (Column column : columns) {
                        if (column.getWidth() <= 0) continue;
                        if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                            String variableName = column.getFieldName().replace('.', '_') + "_" + groupName + "_SUM";
                            JRDesignTextField sumField = createTextField("$V{" + variableName + "}", currentX, 0, column.getWidth(), 20, true, 7f);
                            sumField.setStyle(getTransparentStyle(ReportStyles.NUMERIC_STYLE));
                            if (column.hasPattern()) sumField.setPattern(column.getPattern());
                            sumField.setEvaluationTime(EvaluationTimeEnum.GROUP);
                            sumField.setEvaluationGroup(jrGroup);
                            groupHeaderBand.addElement(sumField);
                        }
                        currentX += column.getWidth();
                    }
                } else {
                    JRDesignTextField groupHeaderField = createTextField(group.getHeaderExpression(), indentation, 0, jasperDesign.getColumnWidth() - indentation, 20, false, 7f);
                    groupHeaderField.setStyle((JRStyle) jasperDesign.getStylesMap().get(group.getStyleName()));
                    groupHeaderBand.addElement(groupHeaderField);
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
        JRDesignTextField pageNumberField = new JRDesignTextField();
        pageNumberField.setX(0); pageNumberField.setY(12);
        pageNumberField.setWidth(jasperDesign.getColumnWidth()); pageNumberField.setHeight(20);
        pageNumberField.setExpression(new JRDesignExpression("\"Strona \" + $V{PAGE_NUMBER} + \" z \" + $V{PAGE_COUNT}"));
        pageNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pageNumberField.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        pageNumberField.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        pageNumberField.setFontSize(8f);
        pageFooterBand.addElement(pageNumberField);
        jasperDesign.setPageFooter(pageFooterBand);
    }

    private void buildSummaryBand() {
        // Logika podsumowania, jeśli jest potrzebna
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
                if (style.getBorderWidth() > 0) setBox(jrStyle.getLineBox(), Color.decode(style.getBorderColor()), style.getBorderWidth());
                if (style.getPadding() != null) jrStyle.getLineBox().setPadding(style.getPadding());
                jasperDesign.addStyle(jrStyle);
            }
        }
    }

    private void setBox(JRLineBox box, Color color, float width) {
        box.getTopPen().setLineColor(color); box.getTopPen().setLineWidth(width);
        box.getLeftPen().setLineColor(color); box.getLeftPen().setLineWidth(width);
        box.getBottomPen().setLineColor(color); box.getBottomPen().setLineWidth(width);
        box.getRightPen().setLineColor(color); box.getRightPen().setLineWidth(width);
    }

    private JRStyle getTransparentStyle(String baseStyleName) throws JRException {
        String transparentStyleName = baseStyleName + "_Transparent";
        JRStyle transparentStyle = (JRStyle) jasperDesign.getStylesMap().get(transparentStyleName);
        if (transparentStyle == null) {
            JRStyle baseStyle = (JRStyle) jasperDesign.getStylesMap().get(baseStyleName);
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
}