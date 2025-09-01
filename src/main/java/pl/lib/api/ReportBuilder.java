// Java
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

public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private final List<Style> styles = new ArrayList<>();
    private final JasperDesign jasperDesign;
    private final Map<String, Object> parameters = new HashMap<>();
    private List<Subreport> subreports = new ArrayList<>();
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

    public ReportBuilder withPageSize(int width, int height) {
        this.jasperDesign.setPageWidth(width);
        this.jasperDesign.setPageHeight(height);
        return this;
    }

    public ReportBuilder withHorizontalLayout() {
        int temp = this.jasperDesign.getPageWidth();
        this.jasperDesign.setPageWidth(this.jasperDesign.getPageHeight());
        this.jasperDesign.setPageHeight(temp);
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

    public ReportBuilder addSubreport(Subreport subreport) {
        if (subreport != null) this.subreports.add(subreport);
        return this;
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

    public ReportBuilder setForSubreport(boolean isForSubreport) {
        this.isForSubreport = isForSubreport;
        return this;
    }

    public JasperReport build() throws JRException {
        setupPage();
        calculateColumnWidths();

        buildStyles();
        declareParameters();
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
        jasperDesign.setColumnWidth(jasperDesign.getPageWidth() - jasperDesign.getLeftMargin() - jasperDesign.getRightMargin());
    }

    private void declareFields() throws JRException {
        for (Column column : columns) {
            if (jasperDesign.getFieldsMap().get(column.getFieldName()) == null) {
                JRDesignField field = new JRDesignField();
                field.setName(column.getFieldName());
                field.setValueClass(column.getType().getJavaClass());
                this.jasperDesign.addField(field);
            }
        }
        for (Group group : groups) {
            if (jasperDesign.getFieldsMap().get(group.getFieldName()) == null) {
                JRDesignField field = new JRDesignField();
                field.setName(group.getFieldName());
                field.setValueClass(String.class);
                this.jasperDesign.addField(field);
            }
        }
    }

    private void declareParameters() throws JRException {
        addParameterIfNotExists("ReportTitle", String.class);
        addParameterIfNotExists("CompanyName", String.class);
        addParameterIfNotExists("CompanyAddress", String.class);
        addParameterIfNotExists("CompanyPostalCode", String.class);
        addParameterIfNotExists("CompanyCity", String.class);

        for (Subreport subreport : this.subreports) {
            if (subreport.getSubreportObjectParameterName() != null) {
                addParameterIfNotExists(subreport.getSubreportObjectParameterName(), JasperReport.class);
            }
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

            if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                String variableName = column.getFieldName() + "_REPORT_SUM";
                if (jasperDesign.getVariablesMap().get(variableName) == null) {
                    JRDesignVariable variable = new JRDesignVariable();
                    variable.setName(variableName);
                    variable.setValueClass(column.getType().getJavaClass());
                    variable.setResetType(ResetTypeEnum.REPORT);
                    variable.setCalculation(toJasperCalculation(column.getReportCalculation()));
                    variable.setExpression(new JRDesignExpression("$F{" + column.getFieldName() + "}"));
                    jasperDesign.addVariable(variable);
                }
            }

            if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                for (Group group : this.groups) {
                    String groupName = "Group_" + group.getFieldName();
                    String variableName = column.getFieldName() + "_" + groupName + "_SUM";

                    JRGroup jrGroup = jasperDesign.getGroupsMap().get(groupName);

                    if (jrGroup != null && jasperDesign.getVariablesMap().get(variableName) == null) {
                        JRDesignVariable variable = new JRDesignVariable();
                        variable.setName(variableName);
                        variable.setValueClass(column.getType().getJavaClass());
                        variable.setResetType(ResetTypeEnum.GROUP);
                        variable.setResetGroup(jrGroup);
                        variable.setCalculation(toJasperCalculation(column.getGroupCalculation()));
                        variable.setExpression(new JRDesignExpression("$F{" + column.getFieldName() + "}"));
                        jasperDesign.addVariable(variable);
                    }
                }
            }
        }
    }

    private CalculationEnum toJasperCalculation(Calculation calc) {
        return (calc == Calculation.SUM) ? CalculationEnum.SUM : CalculationEnum.NOTHING;
    }

    private void buildTitleBand() {
        if (isForSubreport) {
            jasperDesign.setTitle(null);
            return;
        }

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
        if (columns.stream().allMatch(c -> c.getWidth() == 0)) return;

        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(20);
        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() == 0) continue;
            JRDesignStaticText headerText = new JRDesignStaticText();
            headerText.setX(currentX);
            headerText.setY(0);
            headerText.setWidth(column.getWidth());
            headerText.setHeight(20);
            headerText.setText(column.getTitle());
            headerText.setStyle((JRStyle) jasperDesign.getStylesMap().get(ReportStyles.HEADER_STYLE));
            columnHeaderBand.addElement(headerText);
            currentX += column.getWidth();
        }
        jasperDesign.setColumnHeader(columnHeaderBand);
    }

    private void buildDetailBand() throws JRException {
        JRDesignSection detailSection = (JRDesignSection) jasperDesign.getDetailSection();

        if (columns.stream().anyMatch(c -> c.getWidth() != 0)) {
            JRDesignBand dataBand = new JRDesignBand();
            dataBand.setHeight(20);
            int currentX = 0;
            for (Column column : columns) {
                if (column.getWidth() == 0) continue;
                JRDesignTextField dataField = createTextField("$F{" + column.getFieldName() + "}", currentX, 0, column.getWidth(), 20, false, 7f);
                dataField.setStyle((JRStyle) jasperDesign.getStylesMap().get(column.getStyleName()));
                dataField.setStretchWithOverflow(true);
                dataField.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
                dataField.setBlankWhenNull(true);
                if (column.hasPattern()) {
                    dataField.setPattern(column.getPattern());
                }
                dataBand.addElement(dataField);
                currentX += column.getWidth();
            }
            detailSection.addBand(dataBand);
        }

        for (Subreport subreport : subreports) {
            JRDesignBand subreportBand = new JRDesignBand();
            subreportBand.setHeight(1);
            JRDesignSubreport jrSubreport = new JRDesignSubreport(jasperDesign);

            int mainReportWidth = jasperDesign.getColumnWidth();
            int subreportWidth = subreport.getSubreport().getColumnWidth();

            jrSubreport.setX(mainReportWidth - subreportWidth);
            jrSubreport.setY(0);
            jrSubreport.setWidth(subreportWidth);
            jrSubreport.setHeight(1);
            jrSubreport.setExpression(new JRDesignExpression("$P{" + subreport.getSubreportObjectParameterName() + "}"));

            try {
                jrSubreport.addParameter(createSubreportParameter("REPORT_DATA_SOURCE", subreport.getDataSourceExpression()));
                jrSubreport.addParameter(createSubreportParameter("CompanyName", "$P{CompanyName}"));
                jrSubreport.addParameter(createSubreportParameter("CompanyAddress", "$P{CompanyAddress}"));
                jrSubreport.addParameter(createSubreportParameter("CompanyPostalCode", "$P{CompanyPostalCode}"));
                jrSubreport.addParameter(createSubreportParameter("CompanyCity", "$P{CompanyCity}"));
            } catch (JRException e) {
                throw new RuntimeException("Error adding parameters to subreport", e);
            }

            subreportBand.addElement(jrSubreport);
            detailSection.addBand(subreportBand);
        }
    }

    private JRDesignSubreportParameter createSubreportParameter(String name, String expression) {
        JRDesignSubreportParameter param = new JRDesignSubreportParameter();
        param.setName(name);
        param.setExpression(new JRDesignExpression(expression));
        return param;
    }


    private void calculateColumnWidths() {
        int availableWidth = jasperDesign.getColumnWidth();
        List<Column> visibleColumns = columns.stream().filter(c -> c.getWidth() != 0).collect(Collectors.toList());
        int fixedWidthTotal = visibleColumns.stream()
                .filter(c -> c.getWidth() > 0)
                .mapToInt(Column::getWidth).sum();

        final int numericAutoWidth = 82;
        List<Column> numericAutoWidthColumns = visibleColumns.stream()
                .filter(c -> c.getWidth() < 0 && c.getType().isNumeric())
                .collect(Collectors.toList());
        int numericAutoWidthTotal = numericAutoWidthColumns.size() * numericAutoWidth;

        List<Column> otherAutoWidthColumns = visibleColumns.stream()
                .filter(c -> c.getWidth() < 0 && !c.getType().isNumeric())
                .collect(Collectors.toList());

        int remainingWidth = availableWidth - fixedWidthTotal - numericAutoWidthTotal;

        int otherAutoColumnWidth = 100;
        if (!otherAutoWidthColumns.isEmpty() && remainingWidth > 0) {
            otherAutoColumnWidth = remainingWidth / otherAutoWidthColumns.size();
        }

        List<Column> updatedColumns = new ArrayList<>();
        for (Column c : this.columns) {
            if (c.getWidth() < 0) {
                if (c.getType().isNumeric()) {
                    updatedColumns.add(c.withWidth(numericAutoWidth));
                } else {
                    updatedColumns.add(c.withWidth(otherAutoColumnWidth));
                }
            } else {
                updatedColumns.add(c);
            }
        }
        this.columns.clear();
        this.columns.addAll(updatedColumns);
    }

    private void buildGroups() throws JRException {
        int indentationStep = 20;
        for (int i = 0; i < this.groups.size(); i++) {
            Group group = this.groups.get(i);
            String groupName = "Group_" + group.getFieldName();
            JRDesignGroup jrGroup = new JRDesignGroup();
            jrGroup.setName(groupName);
            jrGroup.setExpression(new JRDesignExpression("$F{" + group.getFieldName() + "}"));

            jrGroup.setMinHeightToStartNewPage(21);
    
            if (group.isShowGroupHeader()) {
                JRDesignBand groupHeaderBand = new JRDesignBand();
                groupHeaderBand.setHeight(20);
                int indentation = i * indentationStep;
    
                boolean isSummaryRow = group.isShowGroupFooter();
    
                if (isSummaryRow) {
                    JRDesignStaticText background = new JRDesignStaticText();
                    background.setX(0);
                    background.setY(0);
                    background.setWidth(jasperDesign.getColumnWidth());
                    background.setHeight(20);
                    background.setStyle((JRStyle) jasperDesign.getStylesMap().get(group.getStyleName()));
                    groupHeaderBand.addElement(background);
                }
    
                int firstSumColumnX = jasperDesign.getColumnWidth();
                int currentX = 0;
                for (Column column : columns) {
                    if (column.getWidth() == 0) continue;
                    if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                        firstSumColumnX = currentX;
                        break;
                    }
                    currentX += column.getWidth();
                }
    
                int labelWidth = Math.max(0, firstSumColumnX - indentation);
                if (labelWidth > 0) {
                    JRDesignTextField groupHeaderField = createTextField(group.getHeaderExpression(), indentation, 0, labelWidth, 20, true, 7f);
                    if (isSummaryRow) {
                        groupHeaderField.setStyle(getTransparentStyle(group.getStyleName()));
                    } else {
                        groupHeaderField.setStyle((JRStyle) jasperDesign.getStylesMap().get(group.getStyleName()));
                    }
                    groupHeaderBand.addElement(groupHeaderField);
                }
    
                if (isSummaryRow) {
                    JRStyle transparentNumericStyle = getTransparentStyle(ReportStyles.NUMERIC_STYLE);
                    currentX = 0;
                    for (Column column : columns) {
                        if (column.getWidth() == 0) continue;
                        if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                            String variableName = column.getFieldName() + "_" + groupName + "_SUM";
                            JRDesignTextField sumField = createTextField("$V{" + variableName + "}", currentX, 0, column.getWidth(), 20, true, 7f);
                            sumField.setStyle(transparentNumericStyle);
                            JRStyle groupStyle = (JRStyle) jasperDesign.getStylesMap().get(group.getStyleName());
                            if (groupStyle != null && groupStyle.getForecolor() != null) {
                                sumField.setForecolor(groupStyle.getForecolor());
                            }
                            if (column.hasPattern()) {
                                sumField.setPattern(column.getPattern());
                            }
                            sumField.setEvaluationTime(EvaluationTimeEnum.GROUP);
                            sumField.setEvaluationGroup(jrGroup);
                            groupHeaderBand.addElement(sumField);
                        }
                        currentX += column.getWidth();
                    }
                }
                ((JRDesignSection) jrGroup.getGroupHeaderSection()).addBand(groupHeaderBand);
            }

            jasperDesign.addGroup(jrGroup);
        }
    }

    private void buildPageFooterBand() throws JRException {
        if (!pageFooterEnabled || isForSubreport) {return;}
        JRDesignBand pageFooterBand = new JRDesignBand();
        pageFooterBand.setHeight(35);

        JRDesignLine line = new JRDesignLine();
        line.setX(0);
        line.setY(2);
        line.setWidth(jasperDesign.getColumnWidth());
        line.setHeight(1);
        pageFooterBand.addElement(line);

        JRDesignStaticText leftText = new JRDesignStaticText();
        leftText.setX(0);
        leftText.setY(2);
        leftText.setWidth(200);
        leftText.setHeight(30);
        leftText.setText("eBudżet - ZSI \"Sprawny Urząd\"\nBUK Softres - www.softres.pl");
        leftText.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        leftText.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        leftText.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        leftText.setFontSize(8f);
        pageFooterBand.addElement(leftText);

        JRDesignTextField pageNumberField = new JRDesignTextField();
        pageNumberField.setX(jasperDesign.getColumnWidth() - 200);
        pageNumberField.setY(12);
        pageNumberField.setWidth(200);
        pageNumberField.setHeight(20);
        pageNumberField.setExpression(new JRDesignExpression("\"Strona \" + $V{MASTER_CURRENT_PAGE} + \" z \" + $V{MASTER_TOTAL_PAGES}"));
        pageNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pageNumberField.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        pageNumberField.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        pageNumberField.setFontSize(8f);

        pageNumberField.setEvaluationTime(EvaluationTimeEnum.MASTER);
        pageFooterBand.addElement(pageNumberField);

        jasperDesign.setPageFooter(pageFooterBand);
    }

    private void buildSummaryBand() throws JRException {
        boolean hasReportCalculation = columns.stream().anyMatch(c -> c.hasReportCalculation() && c.getReportCalculation().isActive());
        if (!hasReportCalculation) return;

        JRDesignBand summaryBand = new JRDesignBand();
        summaryBand.setHeight(20);

        JRDesignStaticText background = new JRDesignStaticText();
        background.setX(0);
        background.setY(0);
        background.setWidth(jasperDesign.getColumnWidth());
        background.setHeight(20);
        JRStyle headerStyle = (JRStyle) jasperDesign.getStylesMap().get(ReportStyles.HEADER_STYLE);
        background.setStyle(headerStyle);

        JRLineBox box = background.getLineBox();
        box.getTopPen().setLineColor(Color.decode("#050000"));
        box.getLeftPen().setLineColor(Color.decode("#D6D6D6"));
        box.getBottomPen().setLineColor(Color.decode("#D6D6D6"));
        box.getRightPen().setLineColor(Color.decode("#D6D6D6"));

        summaryBand.addElement(background);

        int firstSumColumnX = jasperDesign.getColumnWidth();
        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() == 0) continue;
            if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                firstSumColumnX = Math.min(firstSumColumnX, currentX);
            }
            currentX += column.getWidth();
        }

        JRDesignStaticText summaryLabel = new JRDesignStaticText();
        summaryLabel.setX(0);
        summaryLabel.setY(0);
        summaryLabel.setWidth(firstSumColumnX);
        summaryLabel.setHeight(20);
        summaryLabel.setBold(true);
        summaryLabel.setFontSize(7f);
        if (headerStyle != null && headerStyle.getForecolor() != null) {
            summaryLabel.setForecolor(headerStyle.getForecolor());
        }
        summaryLabel.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        summaryLabel.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        summaryLabel.setMode(ModeEnum.TRANSPARENT);
        summaryLabel.getLineBox().setLeftPadding(5);
        summaryBand.addElement(summaryLabel);

        JRStyle transparentNumericStyle = getTransparentStyle(ReportStyles.NUMERIC_STYLE);
        currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() == 0) continue;
            if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                String variableName = column.getFieldName() + "_REPORT_SUM";
                JRDesignTextField summaryField = createTextField("$V{" + variableName + "}", currentX, 0, column.getWidth(), 20, true, 7f);
                summaryField.setStyle(transparentNumericStyle);
                if (headerStyle != null && headerStyle.getForecolor() != null) {
                    summaryField.setForecolor(headerStyle.getForecolor());
                }
                if (column.hasPattern()) {
                    summaryField.setPattern(column.getPattern());
                }
                summaryBand.addElement(summaryField);
            }
            currentX += column.getWidth();
        }
        jasperDesign.setSummary(summaryBand);
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

                if (style.getBorderWidth() > 0) {
                    setBox(jrStyle.getLineBox(), Color.decode(style.getBorderColor()), style.getBorderWidth());
                }
                if (style.getPadding() != null) {
                    jrStyle.getLineBox().setPadding(style.getPadding());
                }

                jasperDesign.addStyle(jrStyle);
            }
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
        JRStyle transparentStyle = (JRStyle) jasperDesign.getStylesMap().get(transparentStyleName);
        if (transparentStyle == null) {
            JRStyle baseStyle = (JRStyle) jasperDesign.getStylesMap().get(baseStyleName);
            if (baseStyle == null) {
                return null;
            }

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