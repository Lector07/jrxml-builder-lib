package pl.lib.api;

import pl.lib.model.Column;
import pl.lib.model.DataType;

import java.util.ArrayList;
import java.util.List;

public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private String reportTitle = "Default Report Title";
    private int pageWidth = 842;
    private int pageHeight = 595;

    public ReportBuilder withTitle(String title) {
        this.reportTitle = title;
        return this;
    }

    public ReportBuilder withPageSize(int width, int height) {
        this.pageWidth = width;
        this.pageHeight = height;
        return this;
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type) {
        this.columns.add(new Column(fieldName, title, width, type, null));
        return this;
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type, String pattern) {
        this.columns.add(new Column(fieldName, title, width, type, pattern));
        return this;
    }

    public String build() {
        StringBuilder xml = new StringBuilder();
        int columnWidth = this.pageWidth - 40;

        appendReportDefinition(xml, columnWidth);

        appendParameters(xml);
        appendQueryString(xml);
        appendFields(xml);

        appendTitle(xml, columnWidth);
        appendColumnHeader(xml);
        appendDetailBand(xml);

        appendReportFooter(xml);

        return xml.toString();
    }



    private void appendReportDefinition(StringBuilder xml, int columnWidth) {
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
                .append(" xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" name=\"").append(this.reportTitle)
                .append("\" pageWidth=\"").append(this.pageWidth).append("\" pageHeight=\"").append(this.pageHeight)
                .append("\" columnWidth=\"").append(columnWidth).append("\" leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\" ").append("whenNoDataType=\"AllSectionsNoDetail\">");

    }

    private void appendParameters(StringBuilder xml) {
        xml.append("\n\t<parameter name=\"ReportTitle\" class=\"java.lang.String\"/>\n");
    }

    private void appendQueryString(StringBuilder xml) {
        xml.append("\t<queryString><![CDATA[]]></queryString>\n");
    }

    private void appendFields(StringBuilder xml) {
        for (Column column : this.columns) {
            xml.append("\t<field name=\"").append(column.getFieldName()).append("\" class=\"")
                    .append(column.getType().getJavaClass()).append("\"/>\n");
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
        for (Column column : this.columns) {
            xml.append("\t\t\t<staticText>\n");
            xml.append("\t\t\t\t<reportElement mode=\"Opaque\" backcolor=\"#C0C0C0\" x=\"").append(currentX)
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
        int currentX = 0;
        for (Column column : this.columns) {
            String patternAttribute = column.hasPattern() ? " pattern=\"" + column.getPattern() + "\"" : "";

            xml.append("\t\t\t<textField").append(patternAttribute).append(" isStretchWithOverflow=\"true\">\n");

            xml.append("\t\t\t\t<reportElement x=\"").append(currentX)
                    .append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"20\"/>\n");
            xml.append("\t\t\t\t<box padding=\"2\"><pen lineWidth=\"0.5\"/></box>\n");
            if(column.getType() == DataType.INTEGER || column.getType() == DataType.BIG_DECIMAL) {
                xml.append("\t\t\t\t<textElement textAlignment=\"Right\"/>\n");
            }
            xml.append("\t\t\t\t<textFieldExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></textFieldExpression>\n");
            xml.append("\t\t\t</textField>\n");
            currentX += column.getWidth();
        }
        xml.append("\t\t</band>\n\t</detail>\n");
    }

    private void appendReportFooter(StringBuilder xml) {
        xml.append("</jasperReport>");
    }



}