package pl.lib.api;

import pl.lib.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportBuilder {
    private String title = "Raport";
    private int pageWidth = 595;
    private int pageHeight = 842;
    private int leftMargin = 20, rightMargin = 20, topMargin = 20, bottomMargin = 20;

    private boolean zebraStripingEnabled = false;
    private String footerLeftText = "";
    private String footerRightText = "";

    private Group group;
    private final List<Column> columns = new ArrayList<>();
    private final List<Style> styles = new ArrayList<>();
    private final List<Image> imagesInTitle = new ArrayList<>();

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

    public String build() {
        calculateColumnWidths();

        StringBuilder xml = new StringBuilder();
        appendReportHeader(xml);
        appendStyles(xml);
        appendParameters(xml);
        appendFields(xml);
        appendVariables(xml);
        appendGroups(xml);
        appendTitle(xml);
        appendColumnHeader(xml);
        appendDetailBand(xml);
        appendPageFooter(xml);
        appendSummary(xml);
        appendReportFooter(xml);

        return xml.toString();
    }

    private void calculateColumnWidths() {
        int availableWidth = pageWidth - leftMargin - rightMargin;
        int fixedWidthTotal = columns.stream()
                .filter(c -> c.getWidth() > 0)
                .mapToInt(Column::getWidth)
                .sum();

        List<Column> autoWidthColumns = columns.stream()
                .filter(c -> c.getWidth() <= 0)
                .collect(Collectors.toList());

        if (!autoWidthColumns.isEmpty()) {
            int autoColumnWidth = (availableWidth - fixedWidthTotal) / autoWidthColumns.size();
            for (int i = 0; i < columns.size(); i++) {
                Column c = columns.get(i);
                if (c.getWidth() <= 0) {
                    Column updatedColumn = new Column(c.getFieldName(), c.getTitle(), autoColumnWidth, c.getType(),
                            c.getPattern(), c.getReportCalculation(), c.getGroupCalculation(), c.getStyleName());
                    columns.set(i, updatedColumn);
                }
            }
        }
    }

    private void appendReportHeader(StringBuilder xml) {
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" ")
                .append("name=\"").append(title.replaceAll("\\s+", "_")).append("\" ")
                .append("pageWidth=\"").append(pageWidth).append("\" pageHeight=\"").append(pageHeight).append("\" ")
                .append("columnWidth=\"").append(pageWidth - leftMargin - rightMargin).append("\" ")
                .append("leftMargin=\"").append(leftMargin).append("\" rightMargin=\"").append(rightMargin).append("\" ")
                .append("topMargin=\"").append(topMargin).append("\" bottomMargin=\"").append(bottomMargin).append("\" ")
                .append("uuid=\"").append(generateUUID()).append("\" whenNoDataType=\"AllSectionsNoDetail\">\n");
    }

    private void appendStyles(StringBuilder xml) {
        for (Style style : styles) {
            xml.append("\t<style name=\"").append(style.getName()).append("\" ")
                    .append("fontName=\"").append(style.getFontName()).append("\" ")
                    .append("fontSize=\"").append(style.getFontSize()).append("\" ")
                    .append("isBold=\"").append(style.isBold()).append("\" ")
                    .append("forecolor=\"").append(style.getFontColor()).append("\"");

            if (style.getBackColor() != null) {
                xml.append(" mode=\"Opaque\" backcolor=\"").append(style.getBackColor()).append("\"");
            }
            xml.append(">\n");

            if (zebraStripingEnabled && style.getName().equals("ZebraStripeStyle")) {
                xml.append("\t\t<conditionalStyle>\n")
                        .append("\t\t\t<conditionExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></conditionExpression>\n")
                        .append("\t\t\t<style mode=\"Opaque\" backcolor=\"#FFFFFF\"/>\n")
                        .append("\t\t</conditionalStyle>\n");
            }
            xml.append("\t</style>\n");
        }
    }

    private void appendParameters(StringBuilder xml) {
        xml.append("\t<parameter name=\"ReportTitle\" class=\"java.lang.String\"/>\n");
    }

    private void appendFields(StringBuilder xml) {
        xml.append("\t<queryString><![CDATA[]]></queryString>\n");

        for (Column column : columns) {
            xml.append("\t<field name=\"").append(column.getFieldName())
                    .append("\" class=\"").append(column.getType().getJavaClass()).append("\"/>\n");
        }

        if (group != null) {
            boolean groupFieldExists = columns.stream()
                    .anyMatch(col -> col.getFieldName().equals(group.getFieldName()));
            if (!groupFieldExists) {
                xml.append("\t<field name=\"").append(group.getFieldName())
                        .append("\" class=\"java.lang.String\"/>\n");
            }
        }
    }

    private void appendVariables(StringBuilder xml) {
        for (Column column : columns) {
            if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                appendVariable(xml, column, column.getReportCalculation(), "REPORT", null);
            }
            if (group != null && column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                appendVariable(xml, column, column.getGroupCalculation(), "GROUP", group.getFieldName() + "Group");
            }
        }
    }

    private void appendVariable(StringBuilder xml, Column column, Calculation calc, String scope, String resetGroup) {
        String varName = column.getFieldName() + "_" + scope + "_" + calc.name();
        xml.append("\t<variable name=\"").append(varName).append("\" class=\"").append(column.getType().getJavaClass()).append("\" ");
        if (resetGroup != null) {
            xml.append("resetType=\"Group\" resetGroup=\"").append(resetGroup).append("\" ");
        }
        xml.append("calculation=\"").append(calc.getJasperFunctionName()).append("\">\n");
        xml.append("\t\t<variableExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></variableExpression>\n");
        xml.append("\t</variable>\n");
    }

    private void appendGroups(StringBuilder xml) {
        if (group == null) return;

        int columnWidth = pageWidth - leftMargin - rightMargin;
        String groupName = group.getFieldName() + "Group";

        xml.append("\t<group name=\"").append(groupName).append("\">\n")
                .append("\t\t<groupExpression><![CDATA[$F{").append(group.getFieldName()).append("}]]></groupExpression>\n");

        xml.append("\t\t<groupHeader><band height=\"25\">\n")
                .append("\t\t\t\t<textField isStretchWithOverflow=\"true\">\n")
                .append("\t\t\t\t\t<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"0\" y=\"0\" width=\"").append(columnWidth).append("\" height=\"25\"/>\n")
                .append("\t\t\t\t\t<textElement verticalAlignment=\"Middle\"/>\n")
                .append("\t\t\t\t\t<textFieldExpression><![CDATA[").append(group.getHeaderExpression()).append("]]></textFieldExpression>\n")
                .append("\t\t\t\t</textField>\n")
                .append("\t\t</band></groupHeader>\n");

        boolean hasGroupCalculations = columns.stream().anyMatch(c -> c.hasGroupCalculation() && c.getGroupCalculation().isActive());
        if (hasGroupCalculations) {
            xml.append("\t\t<groupFooter><band height=\"25\">\n");
            int currentX = 0;
            for (Column column : columns) {
                if (column.hasGroupCalculation() && column.getGroupCalculation().isActive()) {
                    String varName = column.getFieldName() + "_GROUP_" + column.getGroupCalculation().name();
                    xml.append("\t\t\t\t<textField pattern=\"").append(column.hasPattern() ? column.getPattern() : "").append("\">\n")
                            .append("\t\t\t\t\t<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"").append(currentX).append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"20\"/>\n")
                            .append("\t\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"/>\n")
                            .append("\t\t\t\t\t<textFieldExpression><![CDATA[$V{").append(varName).append("}]]></textFieldExpression>\n")
                            .append("\t\t\t\t</textField>\n");
                }
                currentX += column.getWidth();
            }
            xml.append("\t\t</band></groupFooter>\n");
        }
        xml.append("\t</group>\n");
    }

    private void appendTitle(StringBuilder xml) {
        int columnWidth = pageWidth - leftMargin - rightMargin;
        xml.append("\t<title><band height=\"50\">\n")
                .append("\t\t\t<textField>\n")
                .append("\t\t\t\t<reportElement x=\"0\" y=\"10\" width=\"").append(columnWidth).append("\" height=\"30\"/>\n")
                .append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\">")
                .append("<font size=\"12\" fontName=\"DejaVu Sans\"/>")
                .append("</textElement>\n")
                .append("\t\t\t\t<textFieldExpression><![CDATA[$P{ReportTitle}]]></textFieldExpression>\n")
                .append("\t\t\t</textField>\n");

        for (Image image : imagesInTitle) {
            xml.append("\t\t\t<image>\n")
                    .append("\t\t\t\t<reportElement x=\"").append(image.getX()).append("\" y=\"").append(image.getY())
                    .append("\" width=\"").append(image.getWidth()).append("\" height=\"").append(image.getHeight()).append("\"/>\n")
                    .append("\t\t\t\t<imageExpression><![CDATA[").append(image.getExpression()).append("]]></imageExpression>\n")
                    .append("\t\t\t</image>\n");
        }

        xml.append("\t\t</band></title>\n");
    }

    private void appendColumnHeader(StringBuilder xml) {
        xml.append("\t<columnHeader><band height=\"30\">\n");
        int currentX = 0;
        for (Column column : columns) {
            xml.append("\t\t\t<staticText>\n")
                    .append("\t\t\t\t<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"").append(currentX).append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"25\"/>\n")
                    .append("\t\t\t\t<box><pen lineWidth=\"0.5\"/></box>\n")
                    .append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"/>\n")
                    .append("\t\t\t\t<text><![CDATA[").append(column.getTitle()).append("]]></text>\n")
                    .append("\t\t\t</staticText>\n");
            currentX += column.getWidth();
        }
        xml.append("\t\t</band></columnHeader>\n");
    }

    private void appendDetailBand(StringBuilder xml) {
        xml.append("\t<detail><band height=\"25\" splitType=\"Stretch\">\n");
        int currentX = 0;
        for (Column column : columns) {
            xml.append("\t\t\t<textField isStretchWithOverflow=\"true\"");
            if (column.hasPattern()) {
                xml.append(" pattern=\"").append(column.getPattern()).append("\"");
            }
            xml.append(">\n");

            String styleAttribute = zebraStripingEnabled ? " style=\"ZebraStripeStyle\"" : "";
            if (column.hasStyle()) {
                styleAttribute = " style=\"" + column.getStyleName() + "\"";
            }

            xml.append("\t\t\t\t<reportElement").append(styleAttribute)
                    .append(" x=\"").append(currentX).append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"25\"/>\n")
                    .append("\t\t\t\t<box padding=\"4\"><pen lineWidth=\"0.5\"/></box>\n");

            xml.append("\t\t\t\t<textElement");
            if (column.getType().getJavaClass().matches(".*(Integer|Long|Double|BigDecimal|Short|Float)")) {
                xml.append(" textAlignment=\"Right\"");
            }
            xml.append(" verticalAlignment=\"Middle\"/>\n");

            xml.append("\t\t\t\t<textFieldExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></textFieldExpression>\n")
                    .append("\t\t\t</textField>\n");
            currentX += column.getWidth();
        }
        xml.append("\t\t</band></detail>\n");
    }

    private void appendPageFooter(StringBuilder xml) {
        int columnWidth = pageWidth - leftMargin - rightMargin;
        int rightPartX = columnWidth - 100;

        xml.append("\t<pageFooter><band height=\"27\">\n")
                .append("\t\t\t<line>\n")
                .append("\t\t\t\t<reportElement x=\"0\" y=\"2\" width=\"").append(columnWidth).append("\" height=\"1\"/>\n")
                .append("\t\t\t</line>\n")
                .append("\t\t\t<staticText>\n")
                .append("\t\t\t\t<reportElement x=\"0\" y=\"2\" width=\"200\" height=\"12\"/>\n")
                .append("\t\t\t\t<textElement verticalAlignment=\"Bottom\"><font fontName=\"Arial\" size=\"8\"/></textElement>\n")
                .append("\t\t\t\t<text><![CDATA[").append(this.footerLeftText).append("]]></text>\n")
                .append("\t\t\t</staticText>\n")
                .append("\t\t\t<staticText>\n")
                .append("\t\t\t\t<reportElement x=\"0\" y=\"14\" width=\"200\" height=\"12\"/>\n")
                .append("\t\t\t\t<textElement verticalAlignment=\"Top\"><font fontName=\"Arial\" size=\"8\"/></textElement>\n")
                .append("\t\t\t\t<text><![CDATA[").append(this.footerRightText).append("]]></text>\n")
                .append("\t\t\t</staticText>\n")
                .append("\t\t\t<textField>\n")
                .append("\t\t\t\t<reportElement x=\"").append(rightPartX).append("\" y=\"7\" width=\"50\" height=\"20\"/>\n")
                .append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font fontName=\"Arial\" size=\"8\"/></textElement>\n")
                .append("\t\t\t\t<textFieldExpression><![CDATA[\"Strona \" + $V{PAGE_NUMBER} + \" z \"]]></textFieldExpression>\n")
                .append("\t\t\t</textField>\n")
                .append("\t\t\t<textField evaluationTime=\"Report\">\n")
                .append("\t\t\t\t<reportElement x=\"").append(rightPartX + 50).append("\" y=\"7\" width=\"50\" height=\"20\"/>\n")
                .append("\t\t\t\t<textElement verticalAlignment=\"Middle\"><font fontName=\"Arial\" size=\"8\"/></textElement>\n")
                .append("\t\t\t\t<textFieldExpression><![CDATA[$V{PAGE_NUMBER}]]></textFieldExpression>\n")
                .append("\t\t\t</textField>\n")
                .append("\t\t</band></pageFooter>\n");
    }

    // java
    private void appendSummary(StringBuilder xml) {
        boolean hasReportCalculations = columns.stream()
                .anyMatch(c -> c.hasReportCalculation() && c.getReportCalculation().isActive());
        if (!hasReportCalculations) return;

        xml.append("\t<summary><band height=\"30\">\n");
        int currentX = 0;
        for (Column column : columns) {
            if (column.hasReportCalculation() && column.getReportCalculation().isActive()) {
                String varName = column.getFieldName() + "_REPORT_" + column.getReportCalculation().name();

                xml.append("\t\t\t<textField");
                if (column.hasPattern()) {
                    xml.append(" pattern=\"").append(column.getPattern()).append("\"");
                }
                xml.append(">\n")
                        .append("\t\t\t\t<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"")
                        .append(currentX).append("\" y=\"5\" width=\"").append(column.getWidth())
                        .append("\" height=\"25\"/>\n")
                        .append("\t\t\t\t<box padding=\"4\"><pen lineWidth=\"0.5\"/></box>\n")
                        .append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"/>\n")
                        .append("\t\t\t\t<textFieldExpression><![CDATA[$V{")
                        .append(varName).append("}]]></textFieldExpression>\n")
                        .append("\t\t\t</textField>\n");
            }
            currentX += column.getWidth();
        }
        xml.append("\t\t</band></summary>\n");
    }

    private void appendReportFooter(StringBuilder xml) {
        xml.append("</jasperReport>");
    }
}