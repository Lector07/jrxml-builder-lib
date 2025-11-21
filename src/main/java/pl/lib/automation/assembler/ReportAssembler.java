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
    }

    private void addField(JasperDesign design, String name, Class<?> valueClass) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(valueClass);
        design.addField(field);
    }

    private void buildDetailBand(JasperDesign design, List<ReportElement> elements) throws JRException {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(19);
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
        headerField.setHeight(11);
        headerField.setRemoveLineWhenBlank(true);
        headerField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        headerField.setBold(true);
        headerField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        headerField.setFontSize(9f);
        headerField.setPositionType(PositionTypeEnum.FLOAT);
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
        keyValueField.setHeight(18);
        keyValueField.setRemoveLineWhenBlank(true);
        keyValueField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        keyValueField.setFontName(ReportStyles.FONT_DEJAVU_SANS);
        keyValueField.setFontSize(10f);
        keyValueField.setMarkup("html");
        keyValueField.setPositionType(PositionTypeEnum.FLOAT);
        keyValueField.setHorizontalTextAlign(HorizontalTextAlignEnum.JUSTIFIED);
        keyValueField.setExpression(new JRDesignExpression(
                "\"&nbsp;\".repeat(Math.max(0, $F{level} - 1) * 3 + 3) + \"<b>\" + $F{text} + \":</b> \" + $F{value} + \"<br/>\""
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
                JRDesignChart chart = chartCompiler.compileChart(
                    element.getChartConfig(),
                    element.getRawTableData(),
                    design.getColumnWidth()
                );
                chart.setPositionType(PositionTypeEnum.FLOAT);
                detailBand.addElement(chart);
            }
        }
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
}
