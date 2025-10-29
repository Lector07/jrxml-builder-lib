package pl.lib.automation;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.component.ComponentKey;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.engine.type.*;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ReportConfig;
import pl.lib.config.ReportTheme;
import pl.lib.model.CompanyInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import pl.lib.api.ReportBuilder;


public class AutomatedReportFacade {
    private final JsonReportGenerator jsonReportGenerator;
    private final ReportBuilder reportBuilder;


    public AutomatedReportFacade() {
        this.jsonReportGenerator = new JsonReportGenerator();
    }

    public AutomatedReportFacade(boolean printJrxml) {
        this.jsonReportGenerator = new JsonReportGenerator().withJrxmlPrinting(printJrxml);
    }

    public byte[] generateCompositeReport(String jsonContent, String reportTitle, CompanyInfo companyInfo) throws JRException, IOException {
        JasperPrint mainContentPrint = jsonReportGenerator.generateReport(jsonContent, reportTitle);

        JasperPrint titlePagePrint = createTitlePage(reportTitle, companyInfo);

        JasperPrint tocPagePrint = createTocPage(mainContentPrint);

        List<JasperPrint> printList = new ArrayList<>();
        printList.add(titlePagePrint);
        printList.add(tocPagePrint);
        printList.add(mainContentPrint);

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(SimpleExporterInput.getInstance(printList));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfOutputStream));

        SimpleExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setCreateBatchModeBookmarks(true);
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        return pdfOutputStream.toByteArray();
    }

    private JasperPrint createTitlePage(ReportConfig config) throws JRException {
        ReportBuilder builder = new ReportBuilder("Title_Page")
                .withPageFormat(config.getPageFormat())
                .withHorizontalLayout("LANDSCAPE".equalsIgnoreCase(config.getOrientation()))
                .withMargins(40, 40, 40, 40) // Można użyć config.getMargins()
                .withTheme(config.getTheme() != null ? ReportTheme.valueOf(config.getTheme().toUpperCase()) : ReportTheme.DEFAULT)
                .withColorSettings(config.getColorSettings());

        JasperDesign design = builder.getDesign();

        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(design.getPageHeight() - design.getTopMargin() - design.getBottomMargin());

        CompanyInfo companyInfo = config.getCompanyInfo();
        if (companyInfo != null) {
            titleBand.addElement(createStaticText(companyInfo.getName(), 0, 150, design.getColumnWidth(), 30, 16, true, HorizontalAlignEnum.CENTER));
        }
        titleBand.addElement(createStaticText(config.getTitle(), 0, 350, design.getColumnWidth(), 60, 28, true, HorizontalAlignEnum.CENTER));
        titleBand.addElement(createStaticText("Data wygenerowania: " + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()), 0, titleBand.getHeight() - 40, design.getColumnWidth(), 20, 10, false, HorizontalAlignEnum.RIGHT));

        design.setTitle(titleBand);

        JasperReport report = builder.build();
        return JasperFillManager.fillReport(report, new HashMap<>(), new JREmptyDataSource());
    }

    private JasperPrint createTocPage(JasperPrint mainPrint, ReportConfig config) throws JRException {
        ReportBuilder builder = new ReportBuilder("TOC_Page")
                .withPageFormat(config.getPageFormat())
                .withHorizontalLayout("LANDSCAPE".equalsIgnoreCase(config.getOrientation()))
                .withMargins(40, 40, 40, 40)
                .withTheme(config.getTheme() != null ? ReportTheme.valueOf(config.getTheme().toUpperCase()) : ReportTheme.DEFAULT)
                .withColorSettings(config.getColorSettings());

        JasperDesign design = builder.getDesign();
        design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);

        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(50);
        titleBand.addElement(createStaticText("Spis Treści", 0, 10, design.getColumnWidth(), 30, 22, true, HorizontalAlignEnum.CENTER));
        design.setTitle(titleBand);

        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(design.getPageHeight() - design.getTopMargin() - design.getBottomMargin() - titleBand.getHeight());

        JRDesignComponentElement tocElement = new JRDesignComponentElement(design);
        tocElement.setComponentKey(new ComponentKey("http://jasperreports.sourceforge.net/jasperreports/components", "jr", "toc"));
        tocElement.setX(0);
        tocElement.setY(20);
        tocElement.setWidth(design.getColumnWidth());
        tocElement.setHeight(detailBand.getHeight() - 40);
        detailBand.addElement(tocElement);
        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);

        JasperReport report = builder.build();

        Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_DATA_SOURCE, new JREmptyDataSource(1));
        params.put(JRParameter.TOC_DATA_SOURCE, new JRTocDataSource(mainPrint));
        return JasperFillManager.fillReport(report, params);
    }

    private JRDesignStaticText createStaticText(String text, int x, int y, int w, int h, float fontSize, boolean isBold, HorizontalAlignEnum align) {
        JRDesignStaticText staticText = new JRDesignStaticText();
        staticText.setX(x);
        staticText.setY(y);
        staticText.setWidth(w);
        staticText.setHeight(h);
        staticText.setText(text != null ? text : "");
        staticText.setFontName("DejaVu Sans");
        staticText.setFontSize(fontSize);
        staticText.setBold(isBold);
        staticText.setHorizontalTextAlign(align);
        staticText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        return staticText;
    }
}

