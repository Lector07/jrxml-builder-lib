package pl.lib.api;

import com.lowagie.text.pdf.VerticalText;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import net.sf.jasperreports.engine.base.*;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import pl.lib.model.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;

public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private final List<Style> styles = new ArrayList<>();
    private final JasperDesign jasperDesign;
    private final List<Group> groups = new ArrayList<>();
    private final Map<String, Object> parameters = new HashMap<>();
    private String title = "Raport";
    private int pageWidth = 595;
    private int pageHeight = 842;
    private int leftMargin = 20, rightMargin = 20, topMargin = 20, bottomMargin = 20;
    private String footerLeftText = "";
    private String footerRightText = "";
    private String headerStyleName = null;
    private List<Subreport> subreports = new ArrayList<>();
    private boolean isForSubreport = false;


    private String queryString = "";

    public ReportBuilder(String reportName) {
        this.jasperDesign = new JasperDesign();
        this.jasperDesign.setName(reportName);
        this.jasperDesign.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
        this.jasperDesign.setLanguage("java");
    }

    public ReportBuilder() {
        this(UUID.randomUUID().toString());
    }

    public ReportBuilder exportToJrxml(String filePath) throws JRException {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            JRXmlWriter.writeReport(jasperDesign, fos, "UTF-8");
        } catch (IOException e) {
            throw new JRException("Błąd podczas zapisywania pliku JRXML", e);
        }
        return this;
    }

    public String getJrxmlContent() throws JRException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JRXmlWriter.writeReport(jasperDesign, baos, "UTF-8");
            return baos.toString("UTF-8");
        } catch (IOException e) {
            throw new JRException("Błąd podczas generowania JRXML", e);
        }
    }

    public ReportBuilder addParameter(String name, Object value) {
        parameters.put(name, value);
        return this;
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

    public ReportBuilder withStandardFooter(String leftText, String rightText) {
        this.footerLeftText = leftText;
        this.footerRightText = rightText;
        return this;
    }

    public ReportBuilder withHeaderStyle(String styleName) {
        this.headerStyleName = styleName;
        return this;
    }

    public ReportBuilder withCompanyInfo(String name, String address, String nipOrPostalCode, String phoneOrCity) {
        addParameter("CompanyName", name);
        addParameter("CompanyAddress", address);
        addParameter("CompanyPostalCode", nipOrPostalCode);
        addParameter("CompanyCity", phoneOrCity);

        return this;
    }

    public ReportBuilder withSubreports(List<? extends Subreport> subreports) {
        this.subreports = subreports != null ? new ArrayList<>(subreports) : new ArrayList<>();
        return this;
    }

    public ReportBuilder withQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public ReportBuilder setForSubreport(boolean isForSubreport) {
        this.isForSubreport = isForSubreport;
        return this;
    }

    public ReportBuilder addSubreport(Subreport subreport) {
        if (subreport != null) {
            this.subreports.add(subreport);
        }
        return this;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    private void setTextFieldBackground(JRDesignTextField textField, Color color) {
        textField.setBackcolor(color);
        textField.setMode(ModeEnum.OPAQUE);
    }

    public ReportBuilder addGroup(Group group) {
        this.groups.add(group);
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


    private JRDesignSubreportParameter createSubreportParameter(String name, String expression) {
        JRDesignSubreportParameter param = new JRDesignSubreportParameter();
        param.setName(name);
        JRDesignExpression expr = new JRDesignExpression();
        expr.setText(expression);
        param.setExpression(expr);
        return param;
    }

    public JasperReport build() throws JRException {
        setupPage();
        buildStyles();
        declareFields();
        declareParameters();
        buildGroups();
        declareVariables();


        buildTitleBand();
        buildColumnHeaderBand();
        buildDetailBand();
        buildPageFooter();
        buildSummaryBand();

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
                if (jasperDesign.getFieldsMap().get(column.getFieldName()) == null) {
                    JRDesignField field = new JRDesignField();
                    field.setName(column.getFieldName());
                    field.setValueClass(column.getType().getJavaClass());
                    this.jasperDesign.addField(field);
                }

            }

            for (Group currentGroup : this.groups) {
                if (jasperDesign.getFieldsMap().get(currentGroup.getFieldName()) == null) {
                    JRDesignField groupField = new JRDesignField();
                    groupField.setName(currentGroup.getFieldName());
                    groupField.setValueClass(DataType.STRING.getJavaClass());
                    this.jasperDesign.addField(groupField);
                }
            }

            if (this.queryString != null && !this.queryString.isEmpty()) {
                JRDesignQuery query = new JRDesignQuery();
                query.setLanguage("json");
                query.setText(this.queryString);
                jasperDesign.setQuery(query);
            }


        } catch (JRException e) {
            throw new RuntimeException("Error declaring fields", e);
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
                if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                    String variableName = column.getFieldName() + "_REPORT_SUM";
                    if (jasperDesign.getVariablesMap().get(variableName) == null) {
                        JRDesignVariable reportVariable = new JRDesignVariable();
                        reportVariable.setName(variableName);
                        reportVariable.setValueClass(column.getType().getJavaClass());
                        reportVariable.setResetType(ResetTypeEnum.REPORT);
                        reportVariable.setCalculation(toJasperCalculation(column.getReportCalculation()));
                        JRDesignExpression expression = new JRDesignExpression();
                        expression.setText("$F{" + column.getFieldName() + "}");
                        reportVariable.setExpression(expression);
                        jasperDesign.addVariable(reportVariable);
                    }
                }

                for (Group currentGroup : this.groups) {
                    if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                        String groupName = "Group_" + currentGroup.getFieldName();
                        String variableName = column.getFieldName() + "_" + groupName + "_SUM";

                        if (jasperDesign.getVariablesMap().get(variableName) == null && jasperDesign.getGroupsMap().get(groupName) != null) {
                            JRDesignVariable groupVariable = new JRDesignVariable();
                            groupVariable.setName(variableName);
                            groupVariable.setValueClass(column.getType().getJavaClass());
                            groupVariable.setResetType(ResetTypeEnum.GROUP);
                            groupVariable.setResetGroup((JRDesignGroup) jasperDesign.getGroupsMap().get(groupName));
                            groupVariable.setCalculation(toJasperCalculation(column.getGroupCalculation()));

                            JRDesignExpression groupExpression = new JRDesignExpression();
                            groupExpression.setText("$F{" + column.getFieldName() + "}");
                            groupVariable.setExpression(groupExpression);

                            jasperDesign.addVariable(groupVariable);
                        }
                    }
                }
            }
        } catch (JRException e) {
            throw new RuntimeException("Error declaring variables", e);
        }
    }

    private void buildTitleBand() {
        JRDesignBand titleBand = new JRDesignBand();
        int availableWidth = this.pageWidth - this.leftMargin - this.rightMargin;

        if (isForSubreport) {
            // WERSJA UPROSZCZONA DLA PODRAPORTU
            titleBand.setHeight(40);

            JRDesignTextField titleTextField = new JRDesignTextField();
            titleTextField.setX(0);
            titleTextField.setY(10);
            titleTextField.setWidth(availableWidth);
            titleTextField.setHeight(25);
            titleTextField.setFontName("DejaVu Sans Condensed");
            titleTextField.setForecolor(Color.decode("#FFFFFF"));
            titleTextField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            titleTextField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            titleTextField.setFontSize(12f);
            titleTextField.setBold(true);
            setTextFieldBackground(titleTextField, Color.decode("#2A3F54"));

            JRDesignExpression expression = new JRDesignExpression();
            expression.setText("$P{ReportTitle}");
            titleTextField.setExpression(expression);
            titleBand.addElement(titleTextField);

        } else {
            // WERSJA PEŁNA DLA RAPORTU GŁÓWNEGO (TWÓJ ORYGINALNY KOD)
            titleBand.setHeight(80);

            JRDesignTextField companyNameField = new JRDesignTextField();
            companyNameField.setX(0);
            companyNameField.setY(0);
            companyNameField.setWidth(availableWidth / 2);
            companyNameField.setHeight(18);
            companyNameField.setFontName("DejaVu Sans");
            companyNameField.setBold(true);
            companyNameField.setFontSize(10f);
            JRDesignExpression companyNameExpr = new JRDesignExpression();
            companyNameExpr.setText("$P{CompanyName}");
            companyNameField.setExpression(companyNameExpr);
            titleBand.addElement(companyNameField);

            JRDesignTextField companyAddressField = new JRDesignTextField();
            companyAddressField.setX(0);
            companyAddressField.setY(18);
            companyAddressField.setWidth(availableWidth / 2);
            companyAddressField.setHeight(15);
            companyAddressField.setFontName("DejaVu Sans");
            companyAddressField.setFontSize(9f);
            JRDesignExpression companyAddressExpr = new JRDesignExpression();
            companyAddressExpr.setText("$P{CompanyAddress}");
            companyAddressField.setExpression(companyAddressExpr);
            titleBand.addElement(companyAddressField);

            JRDesignTextField companyCityField = new JRDesignTextField();
            companyCityField.setX(0);
            companyCityField.setY(33);
            companyCityField.setWidth(availableWidth / 2);
            companyCityField.setHeight(15);
            companyCityField.setFontName("DejaVu Sans");
            companyCityField.setFontSize(9f);
            JRDesignExpression companyCityExpr = new JRDesignExpression();
            companyCityExpr.setText("$P{CompanyPostalCode} + \" \" + $P{CompanyCity}");
            companyCityField.setExpression(companyCityExpr);
            titleBand.addElement(companyCityField);

            JRDesignTextField titleTextField = new JRDesignTextField();
            titleTextField.setX(0);
            titleTextField.setY(50);
            titleTextField.setWidth(availableWidth);
            titleTextField.setHeight(25);
            titleTextField.setFontName("DejaVu Sans Condensed");
            titleTextField.setForecolor(Color.decode("#FFFFFF"));
            titleTextField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            titleTextField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            titleTextField.setFontSize(12f);
            setTextFieldBackground(titleTextField, Color.decode("#2A3F54"));

            JRDesignExpression expression = new JRDesignExpression();
            expression.setText("$P{ReportTitle}");
            titleTextField.setExpression(expression);
            titleBand.addElement(titleTextField);
        }

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
            headerText.setBackcolor(Color.decode("#C6D8E4"));
            headerText.setMode(ModeEnum.OPAQUE);
            headerText.setFontName("DejaVu Sans Condensed");

            JRLineBox box = headerText.getLineBox();
            box.setTopPadding(2);
            box.setRightPadding(2);
            box.setBottomPadding(2);
            box.setLeftPadding(2);

            float borderWidth = 1.0f;
            Color borderColor = Color.BLACK;

            box.getTopPen().setLineWidth(borderWidth);
            box.getTopPen().setLineColor(borderColor);
            box.getRightPen().setLineWidth(borderWidth);
            box.getRightPen().setLineColor(borderColor);
            box.getBottomPen().setLineWidth(borderWidth);
            box.getBottomPen().setLineColor(borderColor);
            box.getLeftPen().setLineWidth(borderWidth);
            box.getLeftPen().setLineColor(borderColor);


            headerText.setText(column.getTitle());
            headerText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            headerText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);

            columnHeaderBand.addElement(headerText);
            currentX += column.getWidth();
        }

        jasperDesign.setColumnHeader(columnHeaderBand);
    }

    private void buildDetailBand() throws JRException {
        JRDesignSection detailSection = (JRDesignSection) jasperDesign.getDetailSection();

        JRDesignBand dataBand = new JRDesignBand();
        dataBand.setHeight(30);
        int currentX = 0;

        for(Column column: columns){
            if(column.getWidth() == 0) continue;
            JRDesignTextField dataField = new JRDesignTextField();
            dataField.setX(currentX);
            dataField.setY(0);
            dataField.setWidth(column.getWidth());
            dataField.setHeight(30);
            dataField.setFontName("DejaVu Sans Condensed");
            dataField.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
            dataField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            dataField.setStretchWithOverflow(true);
            dataField.setBlankWhenNull(true);
            dataField.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);

            if(column.getStyleName() != null && jasperDesign.getStylesMap().get(column.getStyleName()) != null) {
                dataField.setStyle(jasperDesign.getStylesMap().get(column.getStyleName()));
            }

            if(column.hasPattern()){
                dataField.setPattern(column.getPattern());
            }

            JRDesignExpression expression = new JRDesignExpression();
            expression.setText("$F{" + column.getFieldName() + "}");
            dataField.setExpression(expression);

            dataBand.addElement(dataField);
            currentX += column.getWidth();

        }
        detailSection.addBand(dataBand);

        for(Subreport subreport : subreports){
            if(subreport.getTargetBand().equalsIgnoreCase("DETAIL")){
                JRDesignBand subreportBand = new JRDesignBand();
                subreportBand.setHeight(30);

                JRDesignSubreport jrSubreport = new JRDesignSubreport(jasperDesign);
                jrSubreport.setX(10);
                jrSubreport.setY(0);
                jrSubreport.setWidth(pageWidth - leftMargin - rightMargin);
                jrSubreport.setHeight(subreportBand.getHeight());

                JRDesignExpression subreportObjectExpression = new JRDesignExpression();
                subreportObjectExpression.setText("$P{" + subreport.getSubreportObjectParameterName() + "}");
                jrSubreport.setExpression(subreportObjectExpression);

                JRDesignSubreportParameter dataSourceParam = new JRDesignSubreportParameter();
                dataSourceParam.setName("REPORT_DATA_SOURCE");
                JRDesignExpression dataSourceExpression = new JRDesignExpression();
                dataSourceExpression.setText(subreport.getDataSourceExpression());
                dataSourceParam.setExpression(dataSourceExpression);

                try{
                    jrSubreport.addParameter(dataSourceParam);
                    jrSubreport.addParameter(createSubreportParameter("CompanyName", "$P{CompanyName}"));
                    jrSubreport.addParameter(createSubreportParameter("CompanyAddress", "$P{CompanyAddress}"));
                    jrSubreport.addParameter(createSubreportParameter("CompanyPostalCode", "$P{CompanyPostalCode}"));
                    jrSubreport.addParameter(createSubreportParameter("CompanyCity", "$P{CompanyCity}"));
                }catch(Exception e){
                    throw new JRException("Error adding parameters to subreport!", e);
                }

                subreportBand.addElement(jrSubreport);
                detailSection.addBand(subreportBand);

            }
        }

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
        if (this.groups.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.groups.size(); i++) {
            Group currentGroup = this.groups.get(i);

            JRDesignGroup jrGroup = new JRDesignGroup();
            String groupName = "Group_" + currentGroup.getFieldName();
            jrGroup.setName(groupName);

            JRDesignExpression groupExpression = new JRDesignExpression();
            groupExpression.setText("$F{" + currentGroup.getFieldName() + "}");
            jrGroup.setExpression(groupExpression);

            JRDesignBand groupHeaderBand = new JRDesignBand();
            groupHeaderBand.setHeight(25);

            JRDesignTextField groupHeader = new JRDesignTextField();

            int indentation = i * 20;
            groupHeader.setX(indentation);
            groupHeader.setWidth(jasperDesign.getPageWidth() - leftMargin - rightMargin - indentation);
            groupHeader.setY(0);
            groupHeader.setHeight(25);
            groupHeader.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);

            JRLineBox box = groupHeader.getLineBox();
            // Dodanie paddingu 3px do nagłówka grupy
            box.setTopPadding(3);
            box.setRightPadding(3);
            box.setBottomPadding(3);
            box.setLeftPadding(3);
            box.getTopPen().setLineWidth(1.0f);
            box.getRightPen().setLineWidth(1.0f);
            box.getBottomPen().setLineWidth(1.0f);
            box.getLeftPen().setLineWidth(1.0f);

            if (currentGroup.hasStyle() && jasperDesign.getStylesMap().containsKey(currentGroup.getStyleName())) {
                JRDesignStyle baseStyle = (JRDesignStyle) jasperDesign.getStylesMap().get(currentGroup.getStyleName());

                JRDesignStyle levelStyle = (JRDesignStyle) baseStyle.clone();

                Color newBackColor = baseStyle.getBackcolor();
                if (newBackColor != null) {
                    for (int j = 0; j < i; j++) {
                        newBackColor = newBackColor.brighter();
                    }
                    levelStyle.setBackcolor(newBackColor);
                }

                if (i > 0) {
                    levelStyle.setBold(false);
                }

                groupHeader.setStyle(levelStyle);
            }

            JRDesignExpression headerExpression = new JRDesignExpression();
            headerExpression.setText(currentGroup.getHeaderExpression());
            groupHeader.setExpression(headerExpression);
            groupHeaderBand.addElement(groupHeader);
            ((JRDesignSection) jrGroup.getGroupHeaderSection()).addBand(groupHeaderBand);

            if (currentGroup.isShowGroupFooter()) {
                boolean hasGroupCalculations = columns.stream()
                        .anyMatch(c -> c.hasGroupCalculation() && c.getGroupCalculation().isActive());

                if (hasGroupCalculations) {
                    JRDesignBand groupFooterBand = new JRDesignBand();
                    groupFooterBand.setHeight(25);
                    calculateColumnWidths();

                    int currentX = 0;
                    for (Column column : columns) {
                        if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                            JRDesignTextField sumField = new JRDesignTextField();
                            sumField.setX(currentX);
                            sumField.setY(0);
                            sumField.setWidth(column.getWidth());
                            sumField.setHeight(20);
                            sumField.setBold(true);
                            sumField.setBackcolor(Color.decode("#C6D8E4"));
                            sumField.setMode(ModeEnum.OPAQUE);
                            sumField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
                            sumField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);

                            if (column.hasPattern()) {
                                sumField.setPattern(column.getPattern());
                            }

                            JRDesignExpression sumExpression = new JRDesignExpression();
                            String variableName = column.getFieldName() + "_" + groupName + "_" + column.getGroupCalculation().name();
                            sumExpression.setText("$V{" + variableName + "}");
                            sumField.setExpression(sumExpression);

                            JRLineBox sumBox = sumField.getLineBox();
                            sumBox.setTopPadding(3);
                            sumBox.setRightPadding(3);
                            sumBox.setBottomPadding(3);
                            sumBox.setLeftPadding(3);
                            sumBox.getTopPen().setLineWidth(1.0f);
                            sumBox.getRightPen().setLineWidth(1.0f);
                            sumBox.getBottomPen().setLineWidth(1.0f);
                            sumBox.getLeftPen().setLineWidth(1.0f);

                            groupFooterBand.addElement(sumField);
                        }
                        currentX += column.getWidth();
                    }

                    ((JRDesignSection) jrGroup.getGroupFooterSection()).addBand(groupFooterBand);
                }
            }

            jasperDesign.addGroup(jrGroup);

            JRDesignSortField sortField = new JRDesignSortField();
            sortField.setName(currentGroup.getFieldName());
            jasperDesign.addSortField(sortField);
        }
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
        pageNumberExpr.setText("\"Strona \" + $V{PAGE_NUMBER}");
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


    private void buildSummaryBand() throws JRException {
        boolean hasReportCalculation = columns.stream().anyMatch(c -> c.hasReportCalculation() && c.getReportCalculation().isActive());
        if (!hasReportCalculation) {
            return;
        }

        JRDesignBand summaryBand = new JRDesignBand();
        summaryBand.setHeight(30);

        JRDesignStaticText summaryLabel = new JRDesignStaticText();
        summaryLabel.setText("Podsumowanie raportu:");
        summaryLabel.setX(0);
        summaryLabel.setY(5);
        summaryLabel.setWidth(150);
        summaryLabel.setHeight(20);
        summaryLabel.setBold(true);
        summaryBand.addElement(summaryLabel);


        int currentX = 0;
        for (Column column : columns) {
            if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                JRDesignTextField summaryField = new JRDesignTextField();
                summaryField.setX(currentX);
                summaryField.setY(5);
                summaryField.setWidth(column.getWidth());
                summaryField.setHeight(20);
                summaryField.setBold(true);
                summaryField.setBackcolor(Color.decode("#C6D8E4"));
                summaryField.setMode(ModeEnum.OPAQUE);
                summaryField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
                summaryField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
                if (column.hasPattern()) {
                    summaryField.setPattern(column.getPattern());
                }

                JRDesignExpression expression = new JRDesignExpression();
                String variableName = column.getFieldName() + "_REPORT_" + column.getReportCalculation().name();
                expression.setText("$V{" + variableName + "}");
                summaryField.setExpression(expression);
                summaryBand.addElement(summaryField);
            }
            currentX += column.getWidth();
        }
        jasperDesign.setSummary(summaryBand);
        buildSubreports("SUMMARY", summaryBand);
    }

    private void buildStyles() throws JRException {
        for (Style style : this.styles) {
            // --- KLUCZOWA ZMIANA: Sprawdź, czy styl już istnieje, zanim go dodasz ---
            if (jasperDesign.getStylesMap().get(style.getName()) == null) {
                JRDesignStyle jrStyle = new JRDesignStyle();
                jrStyle.setName(style.getName());
                jrStyle.setDefault(false);
                jrStyle.setFontName(style.getFontName());
                jrStyle.setFontSize(style.getFontSize());
                jrStyle.setBold(style.isBold());

                if (style.getFontColor() != null) {
                    jrStyle.setForecolor(Color.decode(style.getFontColor()));
                }
                if (style.getBackColor() != null) {
                    jrStyle.setBackcolor(Color.decode(style.getBackColor()));
                    jrStyle.setMode(ModeEnum.OPAQUE);
                }

                jrStyle.setHorizontalTextAlign(HorizontalTextAlignEnum.valueOf(style.getHorizontalAlignment().toUpperCase()));
                jrStyle.setVerticalTextAlign(VerticalTextAlignEnum.valueOf(style.getVerticalAlignment().toUpperCase()));

                if (style.getBorderWidth() > 0) {
                    JRLineBox box = jrStyle.getLineBox();
                    float width = style.getBorderWidth();
                    Color color = Color.decode(style.getBorderColor());

                    box.getTopPen().setLineWidth(width);
                    box.getTopPen().setLineColor(color);
                    box.getRightPen().setLineWidth(width);
                    box.getRightPen().setLineColor(color);
                    box.getBottomPen().setLineWidth(width);
                    box.getBottomPen().setLineColor(color);
                    box.getLeftPen().setLineWidth(width);
                    box.getLeftPen().setLineColor(color);
                }

                if (style.getPadding() != null) {
                    JRLineBox box = jrStyle.getLineBox();
                    int paddingValue = style.getPadding();
                    box.setTopPadding(paddingValue);
                    box.setRightPadding(paddingValue);
                    box.setBottomPadding(paddingValue);
                    box.setLeftPadding(paddingValue);
                }

                jasperDesign.addStyle(jrStyle);
            }
        }
    }

    private void configureTextFieldBox(JRDesignTextField textField, Column column) {
        if (column.hasBox()) {
            JRLineBox box = textField.getLineBox();
            box.setTopPadding(2);
            box.setRightPadding(2);
            box.setBottomPadding(2);
            box.setLeftPadding(2);

            float width = 1f;
            Color color = Color.BLACK;

            box.getTopPen().setLineWidth(width);
            box.getTopPen().setLineColor(color);
            box.getRightPen().setLineWidth(width);
            box.getRightPen().setLineColor(color);
            box.getBottomPen().setLineWidth(width);
            box.getBottomPen().setLineColor(color);
            box.getLeftPen().setLineWidth(width);
            box.getLeftPen().setLineColor(color);
        }
    }

    private void buildSubreports(String bandName, JRDesignBand band) throws JRException {
        if (this.subreports == null || this.subreports.isEmpty()) {
            return;
        }

        if (band == null) {
            boolean hasSubreportsForThisBand = this.subreports.stream().anyMatch(sr -> sr.getTargetBand().equalsIgnoreCase(bandName));
            if (!hasSubreportsForThisBand) {
                return;
            }
            band = new JRDesignBand();
            band.setHeight(0);
            if (bandName.equalsIgnoreCase("title")) jasperDesign.setTitle(band);
            else if (bandName.equalsIgnoreCase("pageHeader")) jasperDesign.setPageHeader(band);
            else if (bandName.equalsIgnoreCase("columnHeader")) jasperDesign.setColumnHeader(band);
            else if (bandName.equalsIgnoreCase("columnFooter")) jasperDesign.setColumnFooter(band);
            else if (bandName.equalsIgnoreCase("pageFooter")) jasperDesign.setPageFooter(band);
            else if (bandName.equalsIgnoreCase("lastPageFooter")) jasperDesign.setLastPageFooter(band);
            else if (bandName.equalsIgnoreCase("summary")) jasperDesign.setSummary(band);
            else if (bandName.equalsIgnoreCase("noData")) jasperDesign.setNoData(band);
            else {
                ((JRDesignSection) jasperDesign.getDetailSection()).addBand(band);
            }
        }

        int yOffset = 0;
        if (band.getElements() != null && band.getElements().length > 0) {
            yOffset = band.getHeight();
        }

        for (Subreport subreport : this.subreports) {
            if (subreport.getTargetBand().equalsIgnoreCase(bandName)) {
                JRDesignSubreport jrSubreport = new JRDesignSubreport(jasperDesign);

                jrSubreport.setX(0);
                jrSubreport.setY(yOffset);
                jrSubreport.setWidth(jasperDesign.getColumnWidth());
                jrSubreport.setHeight(50);

                JRDesignExpression subreportObjectExpression = new JRDesignExpression();
                subreportObjectExpression.setText("$P{" + subreport.getSubreportObjectParameterName() + "}");
                jrSubreport.setExpression(subreportObjectExpression);

                JRDesignSubreportParameter dataSourceParam = new JRDesignSubreportParameter();
                dataSourceParam.setName("REPORT_DATA_SOURCE");
                JRDesignExpression dataSourceExpression = new JRDesignExpression();
                dataSourceExpression.setText(subreport.getDataSourceExpression());
                dataSourceParam.setExpression(dataSourceExpression);

                try {
                    jrSubreport.addParameter(dataSourceParam);
                } catch (JRException e) {
                    throw new RuntimeException("Błąd podczas dodawania parametru do podraportu", e);
                }

                band.addElement(jrSubreport);

                yOffset += jrSubreport.getHeight();
                if (band.getHeight() < yOffset) {
                    band.setHeight(yOffset);
                }
            }
        }
    }
}
