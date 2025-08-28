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
            // Brak sekcji tytułu dla podraportów
            jasperDesign.setTitle(null);
            return;
        }

        int availableWidth = jasperDesign.getColumnWidth();
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(80);

        titleBand.addElement(createTextField("$P{CompanyName}", 0, 0, availableWidth / 2, 18, true, 10f));
        titleBand.addElement(createTextField("$P{CompanyAddress}", 0, 18, availableWidth / 2, 15, false, 9f));
        titleBand.addElement(createTextField("$P{CompanyPostalCode} + \" \" + $P{CompanyCity}", 0, 33, availableWidth / 2, 15, false, 9f));

        JRDesignTextField titleTextField = createTextField("$P{ReportTitle}", 0, 50, availableWidth, 25, true, 12f);
        titleTextField.setForecolor(Color.decode(ReportStyles.COLOR_WHITE));
        titleTextField.setBackcolor(Color.decode(ReportStyles.COLOR_PRIMARY_BACKGROUND));
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
        columnHeaderBand.setHeight(30);
        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() == 0) continue;
            JRDesignStaticText headerText = new JRDesignStaticText();
            headerText.setX(currentX);
            headerText.setY(0);
            headerText.setWidth(column.getWidth());
            headerText.setHeight(25);
            headerText.setBackcolor(Color.decode(ReportStyles.COLOR_TABLE_HEADER_BACKGROUND));
            headerText.setMode(ModeEnum.OPAQUE);
            headerText.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
            headerText.setText(column.getTitle());
            headerText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            headerText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            setBox(headerText.getLineBox(), Color.decode(ReportStyles.COLOR_BLACK), 1.0f);
            columnHeaderBand.addElement(headerText);
            currentX += column.getWidth();
        }
        jasperDesign.setColumnHeader(columnHeaderBand);
    }

    private void buildDetailBand() throws JRException {
        JRDesignSection detailSection = (JRDesignSection) jasperDesign.getDetailSection();

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

        for (Subreport subreport : subreports) {
            JRDesignBand subreportBand = new JRDesignBand();
            subreportBand.setHeight(40);
            JRDesignSubreport jrSubreport = new JRDesignSubreport(jasperDesign);
            jrSubreport.setX(0);
            jrSubreport.setY(0);
            jrSubreport.setWidth(jasperDesign.getColumnWidth());
            jrSubreport.setHeight(subreportBand.getHeight());
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
        List<Column> autoWidthColumns = visibleColumns.stream().filter(c -> c.getWidth() < 0).collect(Collectors.toList());
        int fixedWidthTotal = visibleColumns.stream().filter(c -> c.getWidth() > 0).mapToInt(Column::getWidth).sum();

        if (!autoWidthColumns.isEmpty()) {
            int remainingWidth = availableWidth - fixedWidthTotal;
            int autoColumnWidth = (remainingWidth > 0) ? remainingWidth / autoWidthColumns.size() : 100;
            autoWidthColumns.forEach(c -> c.setWidth(autoColumnWidth));
        }
    }

    private void buildGroups() throws JRException {
        int indentationStep = 20;
        for (int i = 0; i < this.groups.size(); i++) {
            Group group = this.groups.get(i);
            String groupName = "Group_" + group.getFieldName();
            JRDesignGroup jrGroup = new JRDesignGroup();
            jrGroup.setName(groupName);
            jrGroup.setExpression(new JRDesignExpression("$F{" + group.getFieldName() + "}"));

            if (group.isShowGroupHeader()) {
                JRDesignBand groupHeaderBand = new JRDesignBand();
                groupHeaderBand.setHeight(22);
                int indentation = i * indentationStep;
                JRDesignTextField groupHeaderField = createTextField(group.getHeaderExpression(), indentation, 0, jasperDesign.getColumnWidth() - indentation, 22, true, 8f);
                groupHeaderField.setStyle((JRStyle) jasperDesign.getStylesMap().get(group.getStyleName()));
                groupHeaderBand.addElement(groupHeaderField);
                ((JRDesignSection) jrGroup.getGroupHeaderSection()).addBand(groupHeaderBand);
            }

            if (group.isShowGroupFooter()) {
                JRDesignBand groupFooterBand = new JRDesignBand();
                groupFooterBand.setHeight(22);
                int currentX = 0;
                for (Column column : columns) {
                    if (column.getWidth() == 0) continue;
                    if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                        String variableName = column.getFieldName() + "_" + groupName + "_SUM";
                        JRDesignTextField sumField = createTextField("$V{" + variableName + "}", currentX, 0, column.getWidth(), 22, true, 7f);
                        sumField.setBackcolor(new Color(224, 224, 224, 150));
                        sumField.setMode(ModeEnum.OPAQUE);
                        sumField.setStyle((JRStyle) jasperDesign.getStylesMap().get(ReportStyles.NUMERIC_STYLE));
                        if (column.hasPattern()) {
                            sumField.setPattern(column.getPattern());
                        }
                        groupFooterBand.addElement(sumField);
                    }
                    currentX += column.getWidth();
                }
                ((JRDesignSection) jrGroup.getGroupFooterSection()).addBand(groupFooterBand);
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
        pageNumberField.setExpression(new JRDesignExpression("\"Strona \" + $V{PAGE_NUMBER} + \" z \" + $V{PAGE_COUNT}"));
        pageNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pageNumberField.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        pageNumberField.setFontName(ReportStyles.FONT_DEJAVU_SANS_CONDENSED);
        pageNumberField.setFontSize(8f);

        pageNumberField.setEvaluationTime(EvaluationTimeEnum.PAGE);
        pageFooterBand.addElement(pageNumberField);

        jasperDesign.setPageFooter(pageFooterBand);
    }

    private void buildSummaryBand() throws JRException {
        boolean hasReportCalculation = columns.stream().anyMatch(c -> c.hasReportCalculation() && c.getReportCalculation().isActive());
        if (!hasReportCalculation) return;

        JRDesignBand summaryBand = new JRDesignBand();
        summaryBand.setHeight(30);

        JRDesignStaticText summaryLabel = new JRDesignStaticText();
        summaryLabel.setText("Total Summary:");
        summaryLabel.setX(0);
        summaryLabel.setY(5);
        summaryLabel.setWidth(200);
        summaryLabel.setHeight(20);
        summaryLabel.setBold(true);
        summaryLabel.setFontSize(10f);
        summaryLabel.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        summaryBand.addElement(summaryLabel);

        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() == 0) continue;
            if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                String variableName = column.getFieldName() + "_REPORT_SUM";
                JRDesignTextField summaryField = createTextField("$V{" + variableName + "}", currentX, 5, column.getWidth(), 20, true, 9f);
                summaryField.setBackcolor(Color.decode(ReportStyles.COLOR_TABLE_HEADER_BACKGROUND));
                summaryField.setMode(ModeEnum.OPAQUE);
                summaryField.setStyle((JRStyle) jasperDesign.getStylesMap().get(ReportStyles.NUMERIC_STYLE));
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
}