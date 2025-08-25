package pl.lib.api;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.VerticalAlignEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
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
        buildTitleBand();
        buildColumnHeaderBand();
        buildDetailBand();
        buildGroups();

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

            // Upewnij się, że pole do grupowania jest zadeklarowane, nawet jeśli nie jest kolumną
            if (group != null && jasperDesign.getFieldsMap().get(group.getFieldName()) == null) {
                JRDesignField groupField = new JRDesignField();
                groupField.setName(group.getFieldName());
                // Zakładamy typ String jako domyślny dla pól grupujących, które nie są kolumnami.
                // W bardziej zaawansowanym scenariuszu można by rozszerzyć model `Group`, aby zawierał typ.
                groupField.setValueClassName(DataType.STRING.getJavaClass());
                this.jasperDesign.addField(groupField);
            }
        } catch (JRException e) {
            throw new RuntimeException("Error declaring fields", e);
        }
    }

    private void declareParameters() {
        try {
            JRDesignParameter param = new JRDesignParameter();
            param.setName("ReportTitle");
            param.setValueClassName("java.lang.String");
            jasperDesign.addParameter(param);
        } catch (JRException e) {
            throw new RuntimeException("Error declaring parameters", e);
        }
    }

    private void buildTitleBand() {
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(35);

        JRDesignTextField titleTextField = new JRDesignTextField();
        titleTextField.setX(0);
        titleTextField.setY(10);
        titleTextField.setWidth(jasperDesign.getColumnWidth());
        titleTextField.setHeight(25); // Poprawiona wysokość
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
            if (column.getWidth() == 0) continue; // Nie twórz nagłówka dla ukrytych kolumn
            JRDesignStaticText headerText = new JRDesignStaticText();
            headerText.setX(currentX);
            headerText.setY(0);
            headerText.setWidth(column.getWidth());
            headerText.setHeight(25);
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
            if (column.getWidth() == 0) continue; // Nie twórz pola w sekcji detail dla ukrytych kolumn
            JRDesignTextField dataField = new JRDesignTextField();
            dataField.setX(currentX);
            dataField.setY(0);
            dataField.setWidth(column.getWidth());
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
                .filter(c -> c.getWidth() < 0) // Używamy < 0 dla auto-szerokości
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

}