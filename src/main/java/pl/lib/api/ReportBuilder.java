package pl.lib.api;

import pl.lib.model.DataType;

public class ReportBuilder {
    private StringBuilder xml = new StringBuilder();
    private String title = "Raport";
    private int pageWidth = 842;
    private int pageHeight = 595;
    private boolean zebraStripingEnabled = false;
    private String footerLeftText = "";
    private String footerRightText = "";
    private String groupFieldName = null;
    private String groupHeaderExpression = null;
    private boolean horizontalLayout = false;
    private boolean useHorizontalLayout = false;

    private static class Column {
        private final String fieldName;
        private final String headerText;
        private final int width;
        private final DataType dataType;
        private final String pattern;
        private final boolean summed;
        private final boolean summedInGroup;

        public Column(String fieldName, String headerText, int width, DataType dataType,
                      String pattern, boolean summed, boolean summedInGroup) {
            this.fieldName = fieldName;
            this.headerText = headerText;
            this.width = width;
            this.dataType = dataType;
            this.pattern = pattern;
            this.summed = summed;
            this.summedInGroup = summedInGroup;
        }

        public String getFieldName() { return fieldName; }
        public String getHeaderText() { return headerText; }
        public int getWidth() { return width; }
        public DataType getDataType() { return dataType; }
        public String getPattern() { return pattern; }
        public boolean isSummed() { return summed; }
        public boolean isSummedInGroup() { return summedInGroup; }
    }

    private final java.util.List<Column> columns = new java.util.ArrayList<>();

    public ReportBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public ReportBuilder withPageSize(int width, int height) {
        this.pageWidth = width;
        this.pageHeight = height;
        return this;
    }

    public ReportBuilder withHorizontalLayout() {
        this.horizontalLayout = true;
        this.useHorizontalLayout = true;

        int temp = this.pageWidth;
        this.pageWidth = this.pageHeight;
        this.pageHeight = temp;
        return this;
    }

    private String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    public ReportBuilder withZebraStriping() {
        this.zebraStripingEnabled = true;
        return this;
    }

    public ReportBuilder withStandardFooter(String leftText, String rightText) {
        this.footerLeftText = leftText;
        this.footerRightText = rightText;
        return this;
    }

    public ReportBuilder addGroup(String fieldName, String headerExpression) {
        this.groupFieldName = fieldName;
        this.groupHeaderExpression = headerExpression;
        return this;
    }

    public ReportBuilder addColumn(String fieldName, String headerText, int width, DataType dataType) {
        return addColumn(fieldName, headerText, width, dataType, null, false, false);
    }

    public ReportBuilder addColumn(String fieldName, String headerText, int width, DataType dataType, String pattern) {
        return addColumn(fieldName, headerText, width, dataType, pattern, false, false);
    }

    public ReportBuilder addColumn(String fieldName, String headerText, int width, DataType dataType,
                                   String pattern, boolean summed, boolean summedInGroup) {
        columns.add(new Column(fieldName, headerText, width, dataType, pattern, summed, summedInGroup));
        return this;
    }

    public ReportBuilder addColumn(String fieldName, String headerText, DataType dataType) {
        return addColumn(fieldName, headerText, -1, dataType, null, false, false);
    }


    public String build() {
        int leftMargin = 20, rightMargin = 20, topMargin = 20, bottomMargin = 20;
        int columnWidth = pageWidth - leftMargin - rightMargin;

        int fixedWidthTotal = 0;
        int autoWidthCount = 0;

        for (Column column : columns) {
            if (column.getWidth() > 0) {
                fixedWidthTotal += column.getWidth();
            } else {
                autoWidthCount++;
            }
        }

        int autoColumnWidth = autoWidthCount > 0 ? (columnWidth - fixedWidthTotal) / autoWidthCount : 0;

        java.util.List<Column> adjustedColumns = new java.util.ArrayList<>();
        for (Column c : columns) {
            int finalWidth = c.getWidth() > 0 ? c.getWidth() : autoColumnWidth;
            adjustedColumns.add(new Column(
                    c.getFieldName(), c.getHeaderText(), finalWidth,
                    c.getDataType(), c.getPattern(), c.isSummed(), c.isSummedInGroup()
            ));
        }

        columns.clear();
        columns.addAll(adjustedColumns);

        xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" ");
        xml.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        xml.append("xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports ");
        xml.append("http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" name=\"dynamicReport\" ");
        xml.append("pageWidth=\"").append(pageWidth).append("\" pageHeight=\"").append(pageHeight).append("\" ");

        xml.append("columnWidth=\"").append(columnWidth).append("\" ");
        xml.append("leftMargin=\"").append(leftMargin).append("\" ");
        xml.append("rightMargin=\"").append(rightMargin).append("\" ");
        xml.append("topMargin=\"").append(topMargin).append("\" ");
        xml.append("bottomMargin=\"").append(bottomMargin).append("\" ");
        xml.append("uuid=\"").append(java.util.UUID.randomUUID().toString()).append("\" ");
        xml.append("whenNoDataType=\"AllSectionsNoDetail\">\n");

        appendStyles(xml);
        xml.append("\t<parameter name=\"ReportTitle\" class=\"java.lang.String\"/>\n");
        xml.append("\t<queryString><![CDATA[]]></queryString>\n");

        appendFields(xml);
        appendVariables(xml);
        appendGroups(xml);
        appendTitle(xml);
        appendColumnHeader(xml);
        appendDetailBand(xml);

        xml.append("\t<columnFooter>\n");
        xml.append("\t\t<band height=\"20\"/>\n");
        xml.append("\t</columnFooter>\n");

        appendPageFooter(xml);
        appendSummary(xml);
        xml.append("</jasperReport>");

        return xml.toString();
    }

    private void appendStyles(StringBuilder xml) {
        if (!this.zebraStripingEnabled) return;
        xml.append("\t<style name=\"ZebraStripeStyle\" mode=\"Opaque\" backcolor=\"#F0F0F0\">\n")
                .append("\t\t<conditionalStyle>\n")
                .append("\t\t\t<conditionExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></conditionExpression>\n")
                .append("\t\t\t<style mode=\"Opaque\" backcolor=\"#FFFFFF\"/>\n")
                .append("\t\t</conditionalStyle>\n")
                .append("\t</style>\n");
    }

    private void appendFields(StringBuilder xml) {
        for (Column column : columns) {
            xml.append("\t<field name=\"").append(column.getFieldName()).append("\" class=\"");
            switch (column.getDataType()) {
                case STRING:
                    xml.append("java.lang.String");
                    break;
                case INTEGER:
                    xml.append("java.lang.Integer");
                    break;
                case BIG_DECIMAL:
                    xml.append("java.math.BigDecimal");
                    break;
                case DATE:
                    xml.append("java.util.Date");
                    break;
                default:
                    xml.append("java.lang.String");
            }
            xml.append("\"/>\n");
        }
    }

    private void appendVariables(StringBuilder xml) {
        for (Column column : columns) {
            if (column.isSummed()) {
                xml.append("\t<variable name=\"").append(column.getFieldName()).append("_SUM\" class=\"");
                switch (column.getDataType()) {
                    case INTEGER:
                        xml.append("java.lang.Integer");
                        break;
                    case BIG_DECIMAL:
                        xml.append("java.math.BigDecimal");
                        break;
                    default:
                        xml.append("java.lang.Object");
                }
                xml.append("\" calculation=\"Sum\">\n");
                xml.append("\t\t<variableExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></variableExpression>\n");
                xml.append("\t</variable>\n");
            }
        }

        if (groupFieldName != null) {
            for (Column column : columns) {
                if (column.isSummedInGroup()) {
                    xml.append("\t<variable name=\"").append(column.getFieldName()).append("_GROUP_SUM\" class=\"");
                    switch (column.getDataType()) {
                        case INTEGER:
                            xml.append("java.lang.Integer");
                            break;
                        case BIG_DECIMAL:
                            xml.append("java.math.BigDecimal");
                            break;
                        default:
                            xml.append("java.lang.Object");
                    }
                    xml.append("\" resetType=\"Group\" resetGroup=\"").append(groupFieldName).append("Group\" calculation=\"Sum\">\n");
                    xml.append("\t\t<variableExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></variableExpression>\n");
                    xml.append("\t</variable>\n");
                }
            }
        }
    }

    private void appendGroups(StringBuilder xml) {
        if (groupFieldName == null) return;

        int columnWidth = pageWidth - 40;

        xml.append("\t<group name=\"").append(groupFieldName).append("Group\">\n");
        xml.append("\t\t<groupExpression><![CDATA[$F{").append(groupFieldName).append("}]]></groupExpression>\n");

        xml.append("\t\t<groupHeader>\n");
        xml.append("\t\t\t<band height=\"25\">\n");
        if (this.useHorizontalLayout) {
            xml.append("\t\t\t\t<property name=\"com.jaspersoft.studio.layout\" value=\"com.jaspersoft.studio.editor.layout.HorizontalRowLayout\"/>\n");
        }
        xml.append("\t\t\t\t<textField isStretchWithOverflow=\"true\">\n");
        xml.append("\t\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" ");
        xml.append("mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"0\" y=\"0\" width=\"").append(columnWidth).append("\" height=\"25\"/>\n");
        xml.append("\t\t\t\t\t<box leftPadding=\"4\"/>\n");
        xml.append("\t\t\t\t\t<textElement verticalAlignment=\"Middle\">\n");
        xml.append("\t\t\t\t\t\t<font size=\"12\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/>\n");
        xml.append("\t\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t\t<textFieldExpression><![CDATA[").append(groupHeaderExpression).append("]]></textFieldExpression>\n");
        xml.append("\t\t\t\t</textField>\n");
        xml.append("\t\t\t</band>\n");
        xml.append("\t\t</groupHeader>\n");

        boolean hasGroupSums = columns.stream().anyMatch(Column::isSummedInGroup);
        if (hasGroupSums) {
            xml.append("\t\t<groupFooter>\n");
            xml.append("\t\t\t<band height=\"25\">\n");
            xml.append("\t\t\t\t<staticText>\n");
            xml.append("\t\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" x=\"0\" y=\"2\" width=\"100\" height=\"20\"/>\n");
            xml.append("\t\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
            xml.append("\t\t\t\t\t<text><![CDATA[Suma dla grupy:]]></text>\n");
            xml.append("\t\t\t\t</staticText>\n");

            int currentX = 0;
            for (Column column : columns) {
                currentX += column.getWidth();
                if (column.isSummedInGroup()) {
                    xml.append("\t\t\t\t<textField");
                    if (column.getPattern() != null && !column.getPattern().isEmpty()) {
                        xml.append(" pattern=\"").append(column.getPattern()).append("\"");
                    }
                    xml.append(" isStretchWithOverflow=\"true\">\n");

                    xml.append("\t\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"")
                            .append(currentX - column.getWidth()).append("\" y=\"2\" width=\"").append(column.getWidth()).append("\" height=\"20\"/>\n");
                    xml.append("\t\t\t\t\t<box padding=\"3\"><pen lineWidth=\"0.5\"/></box>\n");
                    xml.append("\t\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font size=\"8\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
                    xml.append("\t\t\t\t\t<textFieldExpression><![CDATA[$V{").append(column.getFieldName()).append("_GROUP_SUM}]]></textFieldExpression>\n");
                    xml.append("\t\t\t\t</textField>\n");
                }
            }

            xml.append("\t\t\t</band>\n");
            xml.append("\t\t</groupFooter>\n");
        }

        xml.append("\t</group>\n");
    }

    private void appendTitle(StringBuilder xml) {
        int columnWidth = pageWidth - 40;

        xml.append("\t<title>\n");
        xml.append("\t\t<band height=\"50\">\n");
        xml.append("\t\t\t<textField>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"10\" width=\"").append(columnWidth).append("\" height=\"30\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"><font size=\"12\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[$P{ReportTitle}]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");
        xml.append("\t\t</band>\n");
        xml.append("\t</title>\n");
    }

    private void appendColumnHeader(StringBuilder xml) {
        xml.append("\t<columnHeader>\n");
        xml.append("\t\t<band height=\"30\">\n");
        if (this.useHorizontalLayout) {
            xml.append("\t\t\t<property name=\"com.jaspersoft.studio.layout\" value=\"com.jaspersoft.studio.editor.layout.HorizontalRowLayout\"/>\n");

            for (Column column : columns) {
                xml.append("\t\t\t<staticText>\n");
                xml.append("\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" x=\"0\" y=\"0\" width=\"1\" height=\"25\" backcolor=\"#DEDEDE\" uuid=\"" + generateUUID() + "\"/>\n");
                xml.append("\t\t\t\t<box><pen lineWidth=\"0.5\"/></box>\n");
                xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"><font size=\"10\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
                xml.append("\t\t\t\t<text><![CDATA[").append(column.getHeaderText()).append("]]></text>\n");
                xml.append("\t\t\t</staticText>\n");
            }
        } else {
            int currentX = 0;
            for (Column column : columns) {
                xml.append("\t\t\t<staticText>\n");
                xml.append("\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"")
                        .append(currentX).append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"25\" uuid=\"" + generateUUID() + "\"/>\n");
                xml.append("\t\t\t\t<box><pen lineWidth=\"0.5\"/></box>\n");
                xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"><font size=\"10\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
                xml.append("\t\t\t\t<text><![CDATA[").append(column.getHeaderText()).append("]]></text>\n");
                xml.append("\t\t\t</staticText>\n");
                currentX += column.getWidth();
            }
        }

        xml.append("\t\t</band>\n");
        xml.append("\t</columnHeader>\n");
    }

    private void appendDetailBand(StringBuilder xml) {
        xml.append("\t<detail>\n");
        xml.append("\t\t<band height=\"25\" splitType=\"Stretch\">\n");
        if (this.useHorizontalLayout) {
            xml.append("\t\t\t<property name=\"com.jaspersoft.studio.layout\" value=\"com.jaspersoft.studio.editor.layout.HorizontalRowLayout\"/>\n");
        }


        int currentX = 0;
        for (Column column : columns) {
            String styleAttribute = zebraStripingEnabled ? " style=\"ZebraStripeStyle\"" : "";

            xml.append("\t\t\t<textField");
            if (column.getPattern() != null && !column.getPattern().isEmpty()) {
                xml.append(" pattern=\"").append(column.getPattern()).append("\"");
            }
            xml.append(" isStretchWithOverflow=\"true\">\n");

            xml.append("\t\t\t\t<reportElement").append(styleAttribute)
                    .append(" positionType=\"Float\" stretchType=\"RelativeToTallestObject\" x=\"")
                    .append(currentX).append("\" y=\"0\" width=\"").append(column.getWidth()).append("\" height=\"25\"/>\n");

            xml.append("\t\t\t\t<box padding=\"4\"><pen lineWidth=\"0.5\"/></box>\n");

            xml.append("\t\t\t\t<textElement");
            if (column.getDataType() == DataType.INTEGER || column.getDataType() == DataType.BIG_DECIMAL) {
                xml.append(" textAlignment=\"Right\"");
            }
            xml.append("><font size=\"8\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");

            xml.append("\t\t\t\t<textFieldExpression><![CDATA[$F{").append(column.getFieldName()).append("}]]></textFieldExpression>\n");
            xml.append("\t\t\t</textField>\n");

            currentX += column.getWidth();
        }

        xml.append("\t\t</band>\n");
        xml.append("\t</detail>\n");
    }

    private void appendPageFooter(StringBuilder xml) {
        int columnWidth = pageWidth - 40;

        xml.append("\t<pageFooter>\n");
        xml.append("\t\t<band height=\"27\">\n");
        xml.append("\t\t\t<line>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"2\" width=\"").append(columnWidth).append("\" height=\"1\"/>\n");
        xml.append("\t\t\t</line>\n");

        xml.append("\t\t\t<staticText>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"2\" width=\"200\" height=\"12\"/>\n");
        xml.append("\t\t\t\t<textElement verticalAlignment=\"Bottom\">\n");
        xml.append("\t\t\t\t\t<font fontName=\"DejaVu Sans Condensed\" size=\"8\"/>\n");
        xml.append("\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t<text><![CDATA[").append(this.footerLeftText).append("]]></text>\n");
        xml.append("\t\t\t</staticText>\n");

        xml.append("\t\t\t<staticText>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"14\" width=\"200\" height=\"12\"/>\n");
        xml.append("\t\t\t\t<textElement verticalAlignment=\"Top\">\n");
        xml.append("\t\t\t\t\t<font fontName=\"DejaVu Sans Condensed\" size=\"8\"/>\n");
        xml.append("\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t<text><![CDATA[").append(this.footerRightText).append("]]></text>\n");
        xml.append("\t\t\t</staticText>\n");

        int rightPartX = columnWidth - 100;
        xml.append("\t\t\t<textField>\n");
        xml.append("\t\t\t\t<reportElement x=\"").append(rightPartX).append("\" y=\"7\" width=\"50\" height=\"20\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\">\n");
        xml.append("\t\t\t\t\t<font fontName=\"DejaVu Sans Condensed\" size=\"8\"/>\n");
        xml.append("\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[\"Strona \" + $V{PAGE_NUMBER} + \" z \"]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");

        xml.append("\t\t\t<textField evaluationTime=\"Report\">\n");
        xml.append("\t\t\t\t<reportElement x=\"").append(rightPartX + 50).append("\" y=\"7\" width=\"50\" height=\"20\"/>\n");
        xml.append("\t\t\t\t<textElement verticalAlignment=\"Middle\">\n");
        xml.append("\t\t\t\t\t<font fontName=\"DejaVu Sans Condensed\" size=\"8\"/>\n");
        xml.append("\t\t\t\t</textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[$V{PAGE_NUMBER}]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");

        xml.append("\t\t</band>\n");
        xml.append("\t</pageFooter>\n");
    }

    private void appendSummary(StringBuilder xml) {
        boolean hasSums = columns.stream().anyMatch(Column::isSummed);
        if (!hasSums) return;

        xml.append("\t<summary>\n");

        xml.append("\t\t<band height=\"30\">\n");
        if (this.useHorizontalLayout) {
            xml.append("\t\t\t<property name=\"com.jaspersoft.studio.layout\" value=\"com.jaspersoft.studio.editor.layout.HorizontalRowLayout\"/>\n");
        }

        xml.append("\t\t\t<staticText>\n");
        xml.append("\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" x=\"0\" y=\"5\" width=\"100\" height=\"25\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
        xml.append("\t\t\t\t<text><![CDATA[SUMA:]]></text>\n");
        xml.append("\t\t\t</staticText>\n");

        int currentX = 0;
        for (Column column : columns) {
            currentX += column.getWidth();
            if (column.isSummed()) {
                xml.append("\t\t\t<textField");
                if (column.getPattern() != null && !column.getPattern().isEmpty()) {
                    xml.append(" pattern=\"").append(column.getPattern()).append("\"");
                }
                xml.append(" isStretchWithOverflow=\"true\">\n");
                xml.append("\t\t\t\t<reportElement positionType=\"Float\" stretchType=\"RelativeToTallestObject\" mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"")
                        .append(currentX - column.getWidth()).append("\" y=\"5\" width=\"").append(column.getWidth()).append("\" height=\"25\"/>\n");
                xml.append("\t\t\t\t<box padding=\"3\"><pen lineWidth=\"0.5\"/></box>\n");
                xml.append("\t\t\t\t<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\"><font size=\"8\" isBold=\"true\" pdfFontName=\"DejaVu Sans\" isPdfEmbedded=\"true\"/></textElement>\n");
                xml.append("\t\t\t\t<textFieldExpression><![CDATA[$V{").append(column.getFieldName()).append("_SUM}]]></textFieldExpression>\n");
                xml.append("\t\t\t</textField>\n");
            }
        }

        xml.append("\t\t</band>\n");
        xml.append("\t</summary>\n");
    }
}