package pl.lib.api;

import pl.lib.model.Column;
import pl.lib.model.DataType;

import java.util.ArrayList;
import java.util.List;

public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private String reportTitle = "Default Report Title";
    private int pageWidth = 595;
    private int pageHeight = 842;
    private boolean zebraStripingEnabled = false;

    public ReportBuilder withTitle(String title) {
        this.reportTitle = title;
        return this;
    }

    public ReportBuilder withPageSize(int width, int height) {
        this.pageWidth = width;
        this.pageHeight = height;
        return this;
    }

    public ReportBuilder withZebraStriping() {
        this.zebraStripingEnabled = true;
        return this;
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type) {
        return addColumn(fieldName, title, width, type, null, false);
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type, String pattern) {
        return addColumn(fieldName, title, width, type, pattern, false);
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type, String pattern, boolean summed) {
        if (summed && (type != DataType.INTEGER && type != DataType.BIG_DECIMAL)) {
            throw new IllegalArgumentException("Summation is only supported for numeric types.");
        }
        this.columns.add(new Column(fieldName, title, width, type, pattern, summed));
        return this;
    }

    public String build() {
        StringBuilder xml = new StringBuilder();
        int columnWidth = this.pageWidth - 40;

        appendReportDefinition(xml, columnWidth);
        appendStyles(xml);
        appendParameters(xml);
        appendQueryString(xml);
        appendFields(xml);
        appendVariables(xml);

        appendTitle(xml, columnWidth);
        appendColumnHeader(xml);
        appendDetailBand(xml);
        appendColumnFooter(xml);
        appendSummary(xml);

        appendReportFooter(xml);

        return xml.toString();
    }


    private void appendReportDefinition(StringBuilder xml, int columnWidth) {
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
                .append(" xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\"")
                .append(" name=\"dynamicReport\" pageWidth=\"").append(pageWidth).append("\" pageHeight=\"").append(pageHeight)
                .append("\" columnWidth=\"").append(columnWidth).append("\" leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\"")
                .append(" whenNoDataType=\"AllSectionsNoDetail\">\n");
    }

    private void appendParameters(StringBuilder xml) {
        xml.append("\t<parameter name=\"ReportTitle\" class=\"java.lang.String\"/>\n");
    }

    private void appendQueryString(StringBuilder xml) {
        xml.append("\t<queryString><![CDATA[]]></queryString>\n");
    }

    private void appendStyles(StringBuilder xml) {
        if (!this.zebraStripingEnabled) {
            return;
        }
        xml.append("\t<style name=\"ZebraStripeStyle\" mode=\"Opaque\" backcolor=\"#F0F0F0\">\n");
        xml.append("\t\t<conditionalStyle>\n");
        xml.append("\t\t\t<conditionExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></conditionExpression>\n");
        xml.append("\t\t\t<style style=\"ZebraStripeStyle\"/>\n");
        xml.append("\t\t</conditionalStyle>\n");
        xml.append("\t</style>\n");
    }

    private void appendFields(StringBuilder xml) {
        for (Column column : columns) {
            xml.append("\t<field name=\"").append(column.getFieldName())
                    .append("\" class=\"").append(column.getType().getJavaClass()).append("\"/>\n");
        }
    }

    private void appendVariables(StringBuilder xml) {
        for (Column column : columns) {
            if (column.isSummed()) {
                String variableName = column.getFieldName() + "_SUM";
                xml.append("\t<variable name=\"").append(variableName).append("\" class=\"").append(column.getType().getJavaClass()).append("\" calculation=\"Sum\">\n");
                xml.append("\t\t<variableExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></variableExpression>\n");
                xml.append("\t</variable>\n");
            }
        }
    }

    private void appendTitle(StringBuilder xml, int columnWidth) {
        xml.append("\t<title>\n\t\t<band height=\"50\">\n");
        xml.append("\t\t\t<textField>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"10\" width=\"").append(columnWidth).append("\" height=\"30\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"><font size=\"18\" isBold=\"true\"/></textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[$P{ReportTitle}]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");
        xml.append("\t\t</band>\n\t</title>\n");
    }

    private void appendColumnHeader(StringBuilder xml) {
        xml.append("\t<columnHeader>\n\t\t<band height=\"30\">\n");
        int currentX = 0;
        for (Column column : columns) {
            xml.append("\t\t\t<staticText>\n");
            xml.append("\t\t\t\t<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"").append(currentX)
                    .append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"30\"/>\n");
            xml.append("\t\t\t\t<box><pen lineWidth=\"0.5\"/></box>\n");
            xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"><font isBold=\"true\"/></textElement>\n");
            xml.append("\t\t\t\t<text><![CDATA[").append(column.getTitle()).append("]]></text>\n");
            xml.append("\t\t\t</staticText>\n");
            currentX += column.getWidth();
        }
        xml.append("\t\t</band>\n\t</columnHeader>\n");
    }

    private void appendDetailBand(StringBuilder xml) {
        xml.append("\t<detail>\n\t\t<band height=\"20\" splitType=\"Stretch\">\n");
        String styleAttribute = zebraStripingEnabled ? " style=\"ZebraStripeStyle\"" : "";
        int currentX = 0;
        for (Column column : columns) {
            String patternAttribute = column.hasPattern() ? " pattern=\"" + column.getPattern() + "\"" : "";
            xml.append("\t\t\t<textField").append(patternAttribute).append(" isStretchWithOverflow=\"true\">\n");

            xml.append("\t\t\t\t<reportElement").append(styleAttribute).append(" mode=\"Opaque\"").append(" x=\"").append(currentX)
                    .append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"20\"/>\n");
            xml.append("\t\t\t\t<box padding=\"2\"><pen lineWidth=\"0.5\"/></box>\n");

            if (column.getType() == DataType.INTEGER || column.getType() == DataType.BIG_DECIMAL) {
                xml.append("\t\t\t\t<textElement textAlignment=\"Right\"/>\n");
            }
            xml.append("\t\t\t\t<textFieldExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></textFieldExpression>\n");
            xml.append("\t\t\t</textField>\n");
            currentX += column.getWidth();
        }
        xml.append("\t\t</band>\n\t</detail>\n");
    }

    private void appendColumnFooter(StringBuilder xml) {
        xml.append("\t<columnFooter>\n\t\t<band height=\"20\"/>\n\t</columnFooter>\n");
    }

    private void appendSummary(StringBuilder xml) {
        boolean hasSummedColumns = columns.stream().anyMatch(Column::isSummed);
        if (!hasSummedColumns) return;

        xml.append("\t<summary>\n\t\t<band height=\"30\">\n");
        xml.append("\t\t\t<staticText>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"5\" width=\"100\" height=\"20\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font size=\"12\" isBold=\"true\"/></textElement>\n");
        xml.append("\t\t\t\t<text><![CDATA[SUMA:]]></text>\n");
        xml.append("\t\t\t</staticText>\n");

        int currentX = 0;
        for (Column column : columns) {
            if (column.isSummed()) {
                String variableName = column.getFieldName() + "_SUM";
                String patternAttribute = column.hasPattern() ? " pattern=\"" + column.getPattern() + "\"" : "";
                xml.append("\t\t\t<textField").append(patternAttribute).append(">\n");
                xml.append("\t\t\t\t<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"").append(currentX)
                        .append("\" y=\"5\" width=\"").append(column.getWidth()).append("\" height=\"20\"/>\n");
                xml.append("\t\t\t\t<box padding=\"2\"><pen lineWidth=\"0.5\"/></box>\n");
                xml.append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font isBold=\"true\"/></textElement>\n");
                xml.append("\t\t\t\t<textFieldExpression><![CDATA[$V{").append(variableName).append("}]]></textFieldExpression>\n");
                xml.append("\t\t\t</textField>\n");
            }
            currentX += column.getWidth();
        }
        xml.append("\t\t</band>\n\t</summary>\n");
    }

    private void appendReportFooter(StringBuilder xml) {
        xml.append("</jasperReport>");
    }
}