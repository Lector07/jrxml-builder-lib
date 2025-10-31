package pl.lib.automation;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ReportConfig;
import pl.lib.config.ReportTheme;
import pl.lib.model.CompanyInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AutomatedReportFacade {

    private final JsonReportGenerator jsonReportGenerator;

    public AutomatedReportFacade() {
        this.jsonReportGenerator = new JsonReportGenerator();
    }

    public AutomatedReportFacade(boolean printJrxml) {
        this.jsonReportGenerator = new JsonReportGenerator().withJrxmlPrinting(printJrxml);
    }

    public byte[] generateCompositeReport(String jsonContent, ReportConfig config) throws JRException, IOException {
        JasperPrint mainContentPrint = jsonReportGenerator.generateReport(jsonContent, config.getTitle());
        JasperPrint titlePagePrint = createTitlePage(config.getTitle(), config.getCompanyInfo(), config);
        JasperPrint tocPagePrint = createTocPage(config);

        List<JasperPrint> printList = new ArrayList<>();
        printList.add(titlePagePrint);
        printList.add(tocPagePrint);
        printList.add(mainContentPrint);

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(SimpleExporterInput.getInstance(printList));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfOutputStream));

        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setCreatingBatchModeBookmarks(Boolean.TRUE);
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        return pdfOutputStream.toByteArray();
    }

    private JasperPrint createTitlePage(String reportTitle, CompanyInfo companyInfo, ReportConfig config) throws JRException {
        ReportBuilder builder = new ReportBuilder("Title_Page")
                .withPageFormat(config.getPageFormat())
                .withHorizontalLayout("LANDSCAPE".equalsIgnoreCase(config.getOrientation()))
                .withMargins(40, 40, 40, 40)
                .withTheme(config.getTheme() != null ? ReportTheme.valueOf(config.getTheme().toUpperCase()) : ReportTheme.DEFAULT)
                .withColorSettings(config.getColorSettings());

        JasperDesign design = builder.getDesign();

        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(design.getPageHeight() - design.getTopMargin() - design.getBottomMargin());

        if (companyInfo != null) {
            titleBand.addElement(createStaticText(companyInfo.getName(), 0, 150, design.getColumnWidth(), 30, 16, true, HorizontalTextAlignEnum.CENTER));
        }
        titleBand.addElement(createStaticText(reportTitle, 0, 350, design.getColumnWidth(), 60, 28, true, HorizontalTextAlignEnum.CENTER));
        titleBand.addElement(createStaticText("Data wygenerowania: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 0, titleBand.getHeight() - 40, design.getColumnWidth(), 20, 10, false, HorizontalTextAlignEnum.RIGHT));

        design.setTitle(titleBand);

        JasperReport report = builder.build();
        return JasperFillManager.fillReport(report, new HashMap<>(), new JREmptyDataSource());
    }

    private JasperPrint createTocPage(ReportConfig config) throws JRException {
        ReportBuilder builder = new ReportBuilder("TOC_Page")
                .withPageFormat(config.getPageFormat())
                .withHorizontalLayout("LANDSCAPE".equalsIgnoreCase(config.getOrientation()))
                .withMargins(40, 40, 40, 40)
                .withTheme(config.getTheme() != null ? ReportTheme.valueOf(config.getTheme().toUpperCase()) : ReportTheme.DEFAULT)
                .withColorSettings(config.getColorSettings());

        JasperDesign design = builder.getDesign();
        design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);

        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(100);
        titleBand.addElement(createStaticText("Spis Treści", 0, 10, design.getColumnWidth(), 30, 22, true, HorizontalTextAlignEnum.CENTER));
        titleBand.addElement(createStaticText("Spis treści zostanie wygenerowany automatycznie na podstawie zakładek dokumentu.", 0, 50, design.getColumnWidth(), 20, 10, false, HorizontalTextAlignEnum.CENTER));
        design.setTitle(titleBand);

        JasperReport report = builder.build();
        return JasperFillManager.fillReport(report, new HashMap<>(), new JREmptyDataSource());
    }

    private JRDesignStaticText createStaticText(String text, int x, int y, int w, int h, float fontSize, boolean isBold, HorizontalTextAlignEnum align) {
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