package pl.lib.api;

import pl.lib.model.Column;
import pl.lib.model.DataType;
import pl.lib.model.Group;

import java.util.ArrayList;
import java.util.List;

public class ReportBuilder {
    private final List<Column> columns = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();
    private String reportTitle = "Default Report Title";
    private int pageWidth = 595;
    private int pageHeight = 842;
    private boolean zebraStripingEnabled = false;
    private boolean pageFooterEnabled = false;
    private String footerLeftText = "eBudżet - ZSI \"Sprawny Urząd\"";
    private String footerRightText = "BUK Softre - ww.softres.pl";


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

    public ReportBuilder withStandardFooter(String leftText, String rightText) {
        this.pageFooterEnabled = true;
        if (leftText != null) {
            this.footerLeftText = leftText;
        }
        if (rightText != null) {
            this.footerRightText = rightText;
        }
        return this;
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type) {
        return addColumn(fieldName, title, width, type, null, false, false);
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type, String pattern) {
        return addColumn(fieldName, title, width, type, pattern, false, false);
    }

    public ReportBuilder addColumn(String fieldName, String title, int width, DataType type, String pattern, boolean summed, boolean summedInGroup) {
        if ((summed || summedInGroup) && (type != DataType.INTEGER && type != DataType.BIG_DECIMAL)) {
            throw new IllegalArgumentException("Summation is only supported for numeric types.");
        }
        this.columns.add(new Column(fieldName, title, width, type, pattern, summed, summedInGroup));
        return this;
    }

    public ReportBuilder addGroup(String fieldName, String headerExpression) {
        boolean fieldExists = columns.stream().anyMatch(c -> c.getFieldName().equals(fieldName));
        if (!fieldExists) {
            throw new IllegalArgumentException("Cannot group by a field that is not defined as a column: " + fieldName);
        }
        this.groups.add(new Group(fieldName, headerExpression));
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
        appendGroups(xml);

        appendTitle(xml, columnWidth);
        appendColumnHeader(xml);
        appendDetailBand(xml);
        appendColumnFooter(xml);
        appendPageFooter(xml);
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
        if (!this.zebraStripingEnabled) return;
        xml.append("\t<style name=\"ZebraStripeStyle\" mode=\"Opaque\" backcolor=\"#F0F0F0\">\n")
                .append("\t\t<conditionalStyle>\n")
                .append("\t\t\t<conditionExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></conditionExpression>\n")
                .append("\t\t\t<style/>\n")
                .append("\t\t</conditionalStyle>\n")
                .append("\t</style>\n");
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

            if (column.isSummedInGroup()) {
                if (groups.isEmpty()) {
                    throw new IllegalStateException("Cannot define a group summary variable when no groups are defined.");
                }
                String groupName = groups.get(0).getFieldName() + "Group";
                String variableName = column.getFieldName() + "_GROUP_SUM";

                xml.append("\t<variable name=\"").append(variableName)
                        .append("\" class=\"").append(column.getType().getJavaClass())
                        .append("\" resetType=\"Group\" resetGroup=\"").append(groupName)
                        .append("\" calculation=\"Sum\">\n");
                xml.append("\t\t<variableExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></variableExpression>\n");
                xml.append("\t</variable>\n");
            }
        }
    }

    private void appendGroups(StringBuilder xml) {
        for (pl.lib.model.Group group : this.groups) {
            String groupName = group.getFieldName() + "Group";

            xml.append("\t<group name=\"").append(groupName).append("\">\n");
            xml.append("\t\t<groupExpression><![CDATA[$F{").append(group.getFieldName()).append("}]]></groupExpression>\n");

            // Header
            xml.append("\t\t<groupHeader>\n");
            xml.append("\t\t\t<band height=\"25\">\n");
            xml.append("\t\t\t\t<textField isStretchWithOverflow=\"true\">\n");
            xml.append("\t\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"0\" y=\"0\" width=\"")
                    .append(pageWidth - 40).append("\" height=\"25\"/>\n");
            xml.append("\t\t\t\t\t<box leftPadding=\"4\"/>\n");
            xml.append("\t\t\t\t\t<textElement verticalAlignment=\"Middle\">\n");
            xml.append("\t\t\t\t\t\t<font size=\"12\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/>\n");
            xml.append("\t\t\t\t\t</textElement>\n");
            xml.append("\t\t\t\t\t<textFieldExpression><![CDATA[").append(group.getHeaderExpression()).append("]]></textFieldExpression>\n");
            xml.append("\t\t\t\t</textField>\n");
            xml.append("\t\t\t</band>\n");
            xml.append("\t\t</groupHeader>\n");

            // Footer
            xml.append("\t\t<groupFooter>\n");
            xml.append("\t\t\t<band height=\"25\">\n");
            xml.append("\t\t\t\t<staticText>\n");
            xml.append("\t\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" x=\"0\" y=\"2\" width=\"100\" height=\"20\"/>\n");
            xml.append("\t\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
            xml.append("\t\t\t\t\t<text><![CDATA[Suma dla grupy:]]></text>\n");
            xml.append("\t\t\t\t</staticText>\n");

            int currentX = 0;
            for (Column column : columns) {
                if (column.isSummedInGroup()) {
                    String variableName = column.getFieldName() + "_GROUP_SUM";
                    String patternAttribute = column.hasPattern() ? " pattern=\"" + column.getPattern() + "\"" : "";
                    xml.append("\t\t\t\t<textField").append(patternAttribute).append(" isStretchWithOverflow=\"true\">\n");
                    xml.append("\t\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"")
                            .append(currentX).append("\" y=\"2\" width=\"").append(column.getWidth()).append("\" height=\"20\"/>\n");
                    xml.append("\t\t\t\t\t<box padding=\"3\"><pen lineWidth=\"0.5\"/></box>\n");
                    xml.append("\t\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font size=\"8\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
                    xml.append("\t\t\t\t\t<textFieldExpression><![CDATA[$V{").append(variableName).append("}]]></textFieldExpression>\n");
                    xml.append("\t\t\t\t</textField>\n");
                }
                currentX += column.getWidth();
            }

            xml.append("\t\t\t</band>\n");
            xml.append("\t\t</groupFooter>\n");

            xml.append("\t</group>\n");
        }
    }

    private void appendTitle(StringBuilder xml, int columnWidth) {
        xml.append("\t<title>\n\t\t<band height=\"50\">\n");
        xml.append("\t\t\t<textField>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"10\" width=\"").append(columnWidth).append("\" height=\"30\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\">")
                .append("<font size=\"12\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/>")
                .append("</textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[$P{ReportTitle}]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");
        xml.append("\t\t</band>\n\t</title>\n");
    }

    private void appendColumnHeader(StringBuilder xml) {
        xml.append("\t<columnHeader>\n\t\t<band height=\"30\">\n");
        int currentX = 0;
        for (Column column : columns) {
            xml.append("\t\t\t<staticText>\n");
            xml.append("\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"")
                    .append(currentX).append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"25\"/>\n");
            xml.append("\t\t\t\t<box><pen lineWidth=\"0.5\"/></box>\n");
            xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\">")
                    .append("<font size=\"10\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/>")
                    .append("</textElement>\n");
            xml.append("\t\t\t\t<text><![CDATA[").append(column.getTitle()).append("]]></text>\n");
            xml.append("\t\t\t</staticText>\n");
            currentX += column.getWidth();
        }
        xml.append("\t\t</band>\n\t</columnHeader>\n");
    }

    private void appendDetailBand(StringBuilder xml) {
        xml.append("\t<detail>\n\t\t<band height=\"25\" splitType=\"Stretch\">\n");
        String styleAttribute = zebraStripingEnabled ? " style=\"ZebraStripeStyle\"" : "";
        int currentX = 0;
        for (Column column : columns) {
            String patternAttribute = column.hasPattern() ? " pattern=\"" + column.getPattern() + "\"" : "";
            xml.append("\t\t\t<textField").append(patternAttribute).append(" isStretchWithOverflow=\"true\">\n");
            xml.append("\t\t\t\t<reportElement")
                    .append(styleAttribute)
                    .append(" positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\"")
                    .append(" x=\"").append(currentX).append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"25\"/>\n");
            xml.append("\t\t\t\t<box padding=\"4\"><pen lineWidth=\"0.5\"/></box>\n");

            if (column.getType() == DataType.INTEGER || column.getType() == DataType.BIG_DECIMAL) {
                xml.append("\t\t\t\t<textElement textAlignment=\"Right\">")
                        .append("<font size=\"8\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/>")
                        .append("</textElement>\n");
            } else {
                xml.append("\t\t\t\t<textElement>")
                        .append("<font size=\"8\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/>")
                        .append("</textElement>\n");
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

    private void appendPageFooter(StringBuilder xml) {
        if (!this.pageFooterEnabled) {
            return;
        }

        int columnWidth = pageWidth - 40;

        xml.append("\t<pageFooter>\n");
        xml.append("\t\t<band height=\"27\">\n");
        xml.append("\t\t\t<line>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"2\" width=\"555\" height=\"1\"/>\n");
        xml.append("\t\t\t</line>\n");

        // Tekst statyczny z dokładnym formatem oczekiwanym przez test (\\n)
        xml.append("\t\t\t<staticText>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"2\" width=\"200\" height=\"25\"/>\n");
        xml.append("\t\t\t\t<textElement verticalAlignment=\"Bottom\">\n");
        xml.append("\t\t\t\t\t<font fontName=\"DejaVu Sans Condensed\" size=\"8\"/>\n");
        xml.append("\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t<text><![CDATA[").append(this.footerLeftText).append("\n").append(this.footerRightText).append("]]></text>\n");
        xml.append("\t\t\t</staticText>\n");

        // Numeracja stron: "Strona X z"
        xml.append("\t\t\t<textField>\n");
        xml.append("\t\t\t\t<reportElement x=\"455\" y=\"7\" width=\"50\" height=\"20\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\">\n");
        xml.append("\t\t\t\t\t<font fontName=\"DejaVu Sans Condensed\" size=\"8\"/>\n");
        xml.append("\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[\"Strona \" + $V{PAGE_NUMBER} + \" z \"]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");

        // Druga część numeracji (całkowita liczba stron)
        xml.append("\t\t\t<textField evaluationTime=\"Report\">\n");
        xml.append("\t\t\t\t<reportElement x=\"510\" y=\"7\" width=\"50\" height=\"20\"/>\n");
        xml.append("\t\t\t\t<textElement verticalAlignment=\"Middle\">\n");
        xml.append("\t\t\t\t\t<font fontName=\"DejaVu Sans Condensed\" size=\"8\"/>\n");
        xml.append("\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[$V{PAGE_NUMBER}]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");

        xml.append("\t\t</band>\n");
        xml.append("\t</pageFooter>\n");
    }

    private void appendSummary(StringBuilder xml) {
        boolean hasSummedColumns = columns.stream().anyMatch(Column::isSummed);
        if (!hasSummedColumns) return;

        xml.append("\t<summary>\n\t\t<band height=\"30\">\n");
        int currentX = 0;
        for (Column column : columns) {
            if (column.isSummed()) {
                String variableName = column.getFieldName() + "_SUM";
                String patternAttribute = column.hasPattern() ? " pattern=\"" + column.getPattern() + "\"" : "";
                xml.append("\t\t\t<textField").append(patternAttribute).append(" isStretchWithOverflow=\"true\">\n");
                xml.append("\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"")
                        .append(currentX).append("\" y=\"5\" width=\"").append(column.getWidth()).append("\" height=\"25\"/>\n");
                xml.append("\t\t\t\t<box padding=\"3\"><pen lineWidth=\"0.5\"/></box>\n");
                xml.append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\">")
                        .append("<font size=\"8\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/>")
                        .append("</textElement>\n");
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