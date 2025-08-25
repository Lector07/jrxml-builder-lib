package pl.lib.api;

import com.lowagie.text.pdf.VerticalText;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import pl.lib.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private final List<Style> styles = new ArrayList<>();
    private final List<Image> imagesInTitle = new ArrayList<>();
    private final JasperDesign jasperDesign;
    private String title = "Raport";
    private int pageWidth = 595;
    private int pageHeight = 842;
    private int leftMargin = 20, rightMargin = 20, topMargin = 20, bottomMargin = 20;
    private boolean zebraStripingEnabled = false;
    private String footerLeftText = "";
    private String footerRightText = "";
    private Group group;

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
        this.title = title;
        return this;
    }

    public ReportBuilder withPageSize(int width, int height) {
        this.pageWidth = width;
        this.pageHeight = height;
        return this;
    }

    public ReportBuilder withMargins(int top, int right, int bottom, int left) {
        this.topMargin = top;
        this.rightMargin = right;
        this.bottomMargin = bottom;
        this.leftMargin = left;
        return this;
    }

    public ReportBuilder withHorizontalLayout() {
        int temp = this.pageWidth;
        this.pageWidth = this.pageHeight;
        this.pageHeight = temp;
        return this;
    }

    public ReportBuilder withZebraStriping() {
        this.zebraStripingEnabled = true;
        Style zebraStyle = new Style("ZebraStripeStyle").withColors(null, "#F0F0F0");
        addStyle(zebraStyle);
        return this;
    }

    public ReportBuilder withStandardFooter(String leftText, String rightText) {
        this.footerLeftText = leftText;
        this.footerRightText = rightText;
        return this;
    }


    public ReportBuilder addGroup(Group group) {
        this.group = group;
        return this;
    }

    public ReportBuilder addImageInTitle(Image image) {
        this.imagesInTitle.add(image);
        return this;
    }

    public ReportBuilder addColumn(Column column) {
        this.columns.add(column);
        return this;
    }

    public ReportBuilder addStyle(Style style) {
        if (styles.stream().noneMatch(s -> s.getName().equals(style.getName()))) {
            this.styles.add(style);
        }
        return this;
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public JasperReport build() throws JRException {
        setupPage();
        declareFields();
        declareParameters();
        buildGroups();
        declareVariables();
        buildTitleBand();
        buildColumnHeaderBand();
        buildDetailBand();
        buildPageFooter();

        return JasperCompileManager.compileReport(this.jasperDesign);
    }

    private void setupPage() {
        this.jasperDesign.setPageWidth(this.pageWidth);
        this.jasperDesign.setPageHeight(this.pageHeight);
        this.jasperDesign.setLeftMargin(this.leftMargin);
        this.jasperDesign.setRightMargin(this.rightMargin);
        this.jasperDesign.setTopMargin(this.topMargin);
        this.jasperDesign.setBottomMargin(this.bottomMargin);

    }

    private void declareFields() {
        try {
            for (Column column : columns) {
                JRDesignField field = new JRDesignField();
                field.setName(column.getFieldName());
                field.setValueClassName(column.getType().getJavaClass());

                this.jasperDesign.addField(field);

            }

            if (group != null && jasperDesign.getFieldsMap().get(group.getFieldName()) == null) {
                JRDesignField groupField = new JRDesignField();
                groupField.setName(group.getFieldName());
                groupField.setValueClassName(DataType.STRING.getJavaClass());
                this.jasperDesign.addField(groupField);
            }


        } catch (JRException e) {
            throw new RuntimeException("Error declaring fields", e);
        }
    }

    private void declareParameters() throws JRException {
        try {
            JRDesignParameter param = new JRDesignParameter();
            param.setName("ReportTitle");
            param.setValueClassName("java.lang.String");
            jasperDesign.addParameter(param);
        } catch (JRException e) {
            throw new RuntimeException("Error declaring parameters", e);
        }
    }

    private CalculationEnum toJasperCalculation(Calculation calc) {
        if (calc == null) {
            return CalculationEnum.NOTHING;
        }
        switch (calc) {
            case SUM:
                return CalculationEnum.SUM;
            case AVERAGE:
                return CalculationEnum.AVERAGE;
            case COUNT:
                return CalculationEnum.COUNT;
            case DISTINCT_COUNT:
                return CalculationEnum.DISTINCT_COUNT;
            case HIGHEST:
                return CalculationEnum.HIGHEST;
            case LOWEST:
                return CalculationEnum.LOWEST;
            case STANDARD_DEVIATION:
                return CalculationEnum.STANDARD_DEVIATION;
            case VARIANCE:
                return CalculationEnum.VARIANCE;
            case NONE:
            default:
                return CalculationEnum.NOTHING;
        }
    }

    private void declareVariables() {
        try {
            for (Column column : columns) {
                boolean needsGroupCalc = group != null && column.hasGroupCalculation() && column.getGroupCalculation().isActive();
                boolean needsReportCalc = column.hasReportCalculation() && column.getReportCalculation().isActive();

                if (needsGroupCalc) {
                    JRDesignVariable groupVariable = new JRDesignVariable();
                    String groupName = "Group_" + this.group.getFieldName();
                    groupVariable.setName(column.getFieldName() + "_Group_" + column.getGroupCalculation().name());
                    groupVariable.setValueClassName(column.getType().getJavaClass());
                    groupVariable.setResetType(net.sf.jasperreports.engine.type.ResetTypeEnum.GROUP);
                    groupVariable.setResetGroup((JRDesignGroup) jasperDesign.getGroupsMap().get(groupName));
                    groupVariable.setCalculation(toJasperCalculation(column.getGroupCalculation()));

                    JRDesignExpression groupExpression = new JRDesignExpression();
                    groupExpression.setText("$F{" + column.getFieldName() + "}");
                    groupVariable.setExpression(groupExpression);

                    jasperDesign.addVariable(groupVariable);
                }

                if (needsReportCalc) {
                    JRDesignVariable reportVariable = new JRDesignVariable();
                    reportVariable.setName(column.getFieldName() + "_REPORT_" + column.getReportCalculation().name());
                    reportVariable.setValueClassName(column.getType().getJavaClass());
                    reportVariable.setResetType(net.sf.jasperreports.engine.type.ResetTypeEnum.REPORT);
                    reportVariable.setCalculation(toJasperCalculation(column.getReportCalculation()));

                    JRDesignExpression expression = new JRDesignExpression();
                    expression.setText("$F{" + column.getFieldName() + "}");
                    reportVariable.setExpression(expression);

                    jasperDesign.addVariable(reportVariable);
                }
            }
        } catch (JRException e) {
            throw new RuntimeException("Error declaring variables", e);
        }
    }

    private void buildTitleBand() {
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(35);

        int availableWidth = this.pageWidth - this.leftMargin - this.rightMargin;

        JRDesignTextField titleTextField = new JRDesignTextField();
        titleTextField.setX(0);
        titleTextField.setY(10);
        titleTextField.setWidth(availableWidth);
        titleTextField.setHeight(25);
        titleTextField.setFontName("DejaVu Sans Condensed");
        titleTextField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleTextField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleTextField.setFontSize(12f);

        JRDesignExpression expression = new JRDesignExpression();
        expression.setText("$P{ReportTitle} != null ? $P{ReportTitle} : \"" + this.title + "\"");
        titleTextField.setExpression(expression);

        titleBand.addElement(titleTextField);

        jasperDesign.setTitle(titleBand);
    }

    private void buildColumnHeaderBand() {
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(30);

        calculateColumnWidths();

        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() == 0) continue;
            JRDesignStaticText headerText = new JRDesignStaticText();
            headerText.setX(currentX);
            headerText.setY(0);
            headerText.setWidth(column.getWidth());
            headerText.setHeight(25);
            headerText.setFontName("DejaVu Sans Condensed");

            headerText.setText(column.getTitle());
            headerText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            headerText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);

            columnHeaderBand.addElement(headerText);
            currentX += column.getWidth();
        }

        jasperDesign.setColumnHeader(columnHeaderBand);
    }

    private void buildDetailBand() {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(30);

        int currentX = 0;
        for (Column column : columns) {
            if (column.getWidth() == 0) continue;
            JRDesignTextField dataField = new JRDesignTextField();
            dataField.setX(currentX);
            dataField.setY(0);
            dataField.setWidth(column.getWidth());
            dataField.setFontName("DejaVu Sans Condensed");

            dataField.setHeight(30);

            if (column.hasPattern()) {
                dataField.setPattern(column.getPattern());
            }

            JRDesignExpression expression = new JRDesignExpression();
            expression.setText("$F{" + column.getFieldName() + "}");
            dataField.setExpression(expression);

            detailBand.addElement(dataField);
            currentX += column.getWidth();
        }

        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(detailBand);
    }

    private void calculateColumnWidths() {
        int availableWidth = pageWidth - leftMargin - rightMargin;
        int fixedWidthTotal = columns.stream()
                .filter(c -> c.getWidth() > 0)
                .mapToInt(Column::getWidth)
                .sum();

        List<Column> autoWidthColumns = columns.stream()
                .filter(c -> c.getWidth() < 0)
                .collect(Collectors.toList());

        if (!autoWidthColumns.isEmpty()) {
            int remainingWidth = availableWidth - fixedWidthTotal;
            int autoColumnWidth = (remainingWidth > 0) ? remainingWidth / autoWidthColumns.size() : 0;
            for (Column c : autoWidthColumns) {
                c.setWidth(autoColumnWidth);
            }
        }
    }

    private void buildGroups() throws JRException {
        if (this.group == null) {
            return;
        }

        JRDesignGroup jrGroup = new JRDesignGroup();
        jrGroup.setName("Group_" + this.group.getFieldName());

        JRDesignExpression groupExpression = new JRDesignExpression();
        groupExpression.setText("$F{" + this.group.getFieldName() + "}");
        jrGroup.setExpression(groupExpression);

        JRDesignBand groupBand = new JRDesignBand();
        groupBand.setHeight(30);

        JRDesignTextField groupHeader = new JRDesignTextField();
        groupHeader.setX(0);
        groupHeader.setY(0);
        groupHeader.setWidth(jasperDesign.getColumnWidth());
        groupHeader.setHeight(30);
        groupHeader.setFontName("DejaVu Sans Condensed");
        groupHeader.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        groupHeader.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);

        JRDesignExpression headerExpression = new JRDesignExpression();
        headerExpression.setText(this.group.getHeaderExpression());
        groupHeader.setExpression(headerExpression);

        groupBand.addElement(groupHeader);

        ((JRDesignSection) jrGroup.getGroupHeaderSection()).addBand(groupBand);

        jasperDesign.addGroup(jrGroup);

        JRDesignSortField sortField = new JRDesignSortField();
        sortField.setName(this.group.getFieldName());
        jasperDesign.addSortField(sortField);
    }


    private void buildPageFooter() {
        if ((this.footerLeftText == null || this.footerLeftText.isEmpty()) && (this.footerRightText == null || this.footerRightText.isEmpty())) {
            return;
        }

        JRDesignBand pageFooterBand = new JRDesignBand();
        pageFooterBand.setHeight(35);

        int availableWidth = this.pageWidth - this.leftMargin - this.rightMargin;

        JRDesignLine line = new JRDesignLine();
        line.setX(0);
        line.setY(2);
        line.setWidth(availableWidth);
        line.setHeight(1);
        pageFooterBand.addElement(line);

        JRDesignTextField pageNumberField = new JRDesignTextField();
        pageNumberField.setX(0);
        pageNumberField.setY(11);
        pageNumberField.setWidth(availableWidth);
        pageNumberField.setHeight(20);
        pageNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pageNumberField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        pageNumberField.setFontName("DejaVu Sans Condensed");
        pageNumberField.setFontSize(8f);

        JRDesignExpression pageNumberExpr = new JRDesignExpression();
        pageNumberExpr.setText("\"Strona \" + $V{MASTER_CURRENT_PAGE} + \" z \" + $V{MASTER_TOTAL_PAGES}");
        pageNumberField.setExpression(pageNumberExpr);
        pageFooterBand.addElement(pageNumberField);

        JRDesignStaticText leftText = new JRDesignStaticText();
        leftText.setX(0);
        leftText.setY(2);
        leftText.setWidth(availableWidth / 2);
        leftText.setHeight(30);
        leftText.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        leftText.setFontName("DejaVu Sans Condensed");
        leftText.setFontSize(8f);

        leftText.setText(this.footerLeftText);
        pageFooterBand.addElement(leftText);

        jasperDesign.setPageFooter(pageFooterBand);
    }

}