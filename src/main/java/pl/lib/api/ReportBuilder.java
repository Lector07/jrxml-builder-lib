package pl.lib.api;

public class ReportBuilder {
    private String reportTitle = "Default Report Title";
    private int pageWidth = 842;
    private int pageHeight = 595;
    private String dataFormat = "JSON";

    public ReportBuilder withTitle(String title) {
        this.reportTitle = title;
        return this;
    }

    public ReportBuilder withPageSize(int width, int height) {
        this.pageWidth = width;
        this.pageHeight = height;
        return this;
    }

    public ReportBuilder withDataFormat(String format) {
        this.dataFormat = format;
        return this;
    }


    public String build() {
        StringBuilder xml = new StringBuilder();
        int columnWidth = this.pageWidth - 40;

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + " xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreport http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" name=\"").append(this.reportTitle).append("\" pageWidth=\"").append(this.pageWidth).append("\" pageHeight=\"").append(this.pageHeight).append("\">\n");
        xml.append("\t<queryString language=\"").append(this.dataFormat).append("\"><![CDATA[]]></queryString>\n");
        xml.append("\t<title>\n\t\t<band height=\"50\">\n");
        xml.append("\t\t\t<textField>\n");
        xml.append("\t\t\t\t<reportElement x=\"0\" y=\"10\" width=\"").append(columnWidth).append("\" height=\"30\"/>\n");
        xml.append("\t\t\t\t<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\"><font size=\"18\" isBold=\"true\"/></textElement>\n");
        xml.append("\t\t\t\t<textFieldExpression><![CDATA[\"").append(this.reportTitle).append("\"]]></textFieldExpression>\n");
        xml.append("\t\t\t</textField>\n");
        xml.append("\t\t</band>\n\t</title>\n");

        xml.append("\t<detail><band/></detail>\n");
        xml.append("</jasperReport>");

        return xml.toString();
    }

}
