package pl.lib.automation;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ReportConfig;
import pl.lib.config.ReportTheme;
import pl.lib.model.CompanyInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        List<Map<String, Object>> tocEntries = jsonReportGenerator.extractTocStructure(jsonContent);
        JasperPrint titlePagePrint = createTitlePage(config.getTitle(), config.getCompanyInfo(), config);

        String cityName = config.getCompanyInfo() != null ? config.getCompanyInfo().getName() : "Organizacja";
        JasperPrint mainContentPrint = jsonReportGenerator.generateReport(jsonContent, config.getTitle(), cityName, false);
        List<JasperPrint> printList = new ArrayList<>();
        printList.add(titlePagePrint);
        if (tocEntries != null && !tocEntries.isEmpty()) {
            JasperPrint tocPagePrint = createTocPageFromData(tocEntries, config);
            printList.add(tocPagePrint);
        }
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
                .withMargins(20, 20, 20, 20)
                .withTheme(config.getTheme() != null ? ReportTheme.valueOf(config.getTheme().toUpperCase()) : ReportTheme.DEFAULT)
                .withColorSettings(config.getColorSettings())
                .withTitleBand(false);
        JasperDesign design = builder.getDesign();
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(270); // Y=200 + wysokość=60 + margines=10
        if (companyInfo != null) {
            detailBand.addElement(createStaticText(companyInfo.getName(), 0, 20, design.getColumnWidth(), 30, 16, true, HorizontalTextAlignEnum.CENTER));
        }
        detailBand.addElement(createStaticText(reportTitle, 0, 100, design.getColumnWidth(), 60, 28, true, HorizontalTextAlignEnum.CENTER));

        ((JRDesignSection) design.getDetailSection()).addBand(detailBand);
        JasperReport report = builder.build();
        return JasperFillManager.fillReport(report, new HashMap<>(), new JREmptyDataSource());
    }

    private JasperPrint createTocPageFromData(List<Map<String, Object>> tocEntries, ReportConfig config) throws JRException {
        InputStream tocTemplateStream = getClass().getClassLoader().getResourceAsStream("templates/toc_template.jrxml");
        if (tocTemplateStream == null) {
            throw new JRException("Nie znaleziono szablonu toc_template.jrxml");
        }
        JasperDesign design = JRXmlLoader.load(tocTemplateStream);
        JasperReport report = JasperCompileManager.compileReport(design);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("PAGE_FORMAT", config.getPageFormat());
        parameters.put("ORIENTATION", config.getOrientation());
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource((Collection<Map<String, ?>>) (Collection<?>) tocEntries);
        return JasperFillManager.fillReport(report, parameters, dataSource);
    }

    private JRDesignStaticText createStaticText(String text, int x, int y, int w, int h, float fontSize, boolean isBold, HorizontalTextAlignEnum align) {
        JRDesignStaticText staticText = new JRDesignStaticText();
        staticText.setX(x);
        staticText.setY(y);
        staticText.setWidth(w);
        staticText.setHeight(h);
        staticText.setText(text != null ? text : "");
        staticText.setFontName("DejaVu Sans Condensed");
        staticText.setFontSize(fontSize);
        staticText.setBold(isBold);
        staticText.setHorizontalTextAlign(align);
        staticText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        return staticText;
    }

}