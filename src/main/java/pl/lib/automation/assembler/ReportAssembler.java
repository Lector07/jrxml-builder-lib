package pl.lib.automation.assembler;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import pl.lib.automation.analyzer.ReportElement;
import pl.lib.automation.compiler.ChartCompiler;
import pl.lib.model.ReportStyles;

import java.util.List;
import java.util.Map;

public class ReportAssembler {
    private final ChartCompiler chartCompiler = new ChartCompiler();

    public JasperPrint assemble(
            JasperDesign design,
            JRDataSource dataSource,
            Map<String, Object> parameters,
            List<ReportElement> elements
    ) throws JRException {
        addFieldsToDesign(design);
        buildDetailBand(design, elements);
        buildPageFooter(design);
        JasperReport jasperReport = JasperCompileManager.compileReport(design);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        jasperPrint.setProperty("net.sf.jasperreports.create.bookmarks", "true");
        return jasperPrint;
    }

    private void addFieldsToDesign(JasperDesign design) throws JRException {
        addField(design, "type", String.class);
        addField(design, "text", String.class);
        addField(design, "value", String.class);
        addField(design, "level", Integer.class);
        addField(design, "elementIndex", Integer.class);

        JRDesignParameter footerParam = new JRDesignParameter();
        footerParam.setName("FooterLeftText");
        footerParam.setValueClass(String.class);
        design.addParameter(footerParam);

        JRDesignParameter isTocPageParam = new JRDesignParameter();
        isTocPageParam.setName("IS_TOC_PAGE");
        isTocPageParam.setValueClass(Boolean.class);
        design.addParameter(isTocPageParam);
    }

    private void addField(JasperDesign design, String name, Class<?> valueClass) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(valueClass);
        design.addField(field);
    }

    private void buildDetailBand(JasperDesign design, List<ReportElement> elements) throws JRException {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);
        detailBand.setSplitType(SplitTypeEnum.STRETCH);
        detailBand.addElement(createHeaderField(design));
        detailBand.addElement(createKeyValueField(design));
        addTableSubreports(detailBand, design, elements);
        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);
    }

    private JRDesignTextField createHeaderField(JasperDesign design) {
        JRDesignTextField headerField = new JRDesignTextField();
        headerField.setX(0);
        headerField.setY(0);
        headerField.setWidth(design.getColumnWidth());
        headerField.setHeight(18);
        headerField.setRemoveLineWhenBlank(true);
        headerField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        headerField.setBold(true);
        headerField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        headerField.setFontSize(12f);
        headerField.setPositionType(PositionTypeEnum.FLOAT);
        headerField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        headerField.setForecolor(new java.awt.Color(52, 73, 94));

        headerField.setExpression(new JRDesignExpression(
                "\"   \".repeat(Math.max(0, $F{level} - 1)) + $F{text}"
        ));

        headerField.setPrintWhenExpression(new JRDesignExpression("$F{type}.equals(\"HEADER\")"));
        headerField.setAnchorNameExpression(new JRDesignExpression("\"bookmark_\" + $F{elementIndex}"));
        headerField.setBookmarkLevelExpression(new JRDesignExpression("$F{level}"));
        return headerField;
    }

    private JRDesignTextField createKeyValueField(JasperDesign design) {
        JRDesignTextField keyValueField = new JRDesignTextField();
        keyValueField.setX(0);
        keyValueField.setY(0);
        keyValueField.setWidth(design.getColumnWidth());
        keyValueField.setHeight(20);
        keyValueField.setRemoveLineWhenBlank(true);
        keyValueField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        keyValueField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        keyValueField.setFontSize(10f);
        keyValueField.setMarkup("html");
        keyValueField.setPositionType(PositionTypeEnum.FLOAT);
        keyValueField.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        keyValueField.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        keyValueField.setExpression(new JRDesignExpression(
                "\"&nbsp;\".repeat(Math.max(0, $F{level} - 1) * 4 + 2) + \"<b style='color: #2C3E50;'>\" + $F{text} + \":</b> \" + $F{value}"
        ));
        keyValueField.setPrintWhenExpression(new JRDesignExpression("$F{type}.equals(\"KEY_VALUE\")"));
        return keyValueField;
    }

    private void addTableSubreports(JRDesignBand detailBand, JasperDesign design, List<ReportElement> elements) throws JRException {
        for (int i = 0; i < elements.size(); i++) {
            ReportElement element = elements.get(i);

            if ("TABLE".equals(element.getType())) {
                detailBand.addElement(createTableSubreport(design, i));
            }
            else if ("CHART".equals(element.getType()) && element.getChartConfig() != null) {
                detailBand.addElement(createChartSubreport(design, i));
            }
        }
    }

    private JRDesignSubreport createChartSubreport(JasperDesign design, int index) {
        JRDesignSubreport subreport = new JRDesignSubreport(design);
        subreport.setX(0);
        subreport.setY(0);
        subreport.setWidth(design.getColumnWidth());
        subreport.setHeight(1); // Dynamiczna wysokość - subreport dostosuje się do zawartości
        subreport.setRemoveLineWhenBlank(true);
        subreport.setPositionType(PositionTypeEnum.FLOAT);
        subreport.setExpression(new JRDesignExpression("$P{CHART_REPORT_" + index + "}"));
        subreport.setDataSourceExpression(new JRDesignExpression("$P{CHART_DATA_" + index + "}"));
        subreport.setPrintWhenExpression(new JRDesignExpression(
                "$F{type}.equals(\"CHART\") && $F{elementIndex}.equals(" + index + ")"
        ));
        return subreport;
    }

    private JRDesignSubreport createTableSubreport(JasperDesign design, int index) {
        JRDesignSubreport subreport = new JRDesignSubreport(design);
        subreport.setX(0);
        subreport.setY(0);
        subreport.setWidth(design.getColumnWidth());
        subreport.setHeight(1);
        subreport.setRemoveLineWhenBlank(true);
        subreport.setPositionType(PositionTypeEnum.FLOAT);
        subreport.setExpression(new JRDesignExpression("$P{TABLE_REPORT_" + index + "}"));
        subreport.setDataSourceExpression(new JRDesignExpression("$P{TABLE_DATA_" + index + "}"));
        subreport.setPrintWhenExpression(new JRDesignExpression(
                "$F{type}.equals(\"TABLE\") && $F{elementIndex}.equals(" + index + ")"
        ));
        return subreport;
    }

    private void buildPageFooter(JasperDesign design) throws JRException {
        JRDesignBand pageFooterBand = new JRDesignBand();
        pageFooterBand.setHeight(35);

        JRDesignExpression printWhenExpression = new JRDesignExpression();
        printWhenExpression.setText("($V{PAGE_NUMBER}.intValue() > 1) || Boolean.FALSE.equals($P{IS_TOC_PAGE})");
        pageFooterBand.setPrintWhenExpression(printWhenExpression);

        // Lewa strona - tekst z parametru FooterLeftText
        JRDesignTextField leftText = new JRDesignTextField();
        leftText.setX(0);
        leftText.setY(2);
        leftText.setWidth(design.getColumnWidth() / 2);
        leftText.setHeight(30);
        leftText.setExpression(new JRDesignExpression("$P{FooterLeftText}"));
        leftText.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        leftText.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        leftText.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        leftText.setFontSize(8f);
        pageFooterBand.addElement(leftText);

        // Prawa strona - numer strony
        JRDesignTextField pageNumberField = new JRDesignTextField();
        pageNumberField.setX(0);
        pageNumberField.setY(12);
        pageNumberField.setWidth(design.getColumnWidth());
        pageNumberField.setHeight(20);
        pageNumberField.setExpression(new JRDesignExpression("\"Strona \" + $V{PAGE_NUMBER}"));
        pageNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pageNumberField.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
        pageNumberField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        pageNumberField.setFontSize(8f);
        pageFooterBand.addElement(pageNumberField);

        design.setPageFooter(pageFooterBand);
    }
}
