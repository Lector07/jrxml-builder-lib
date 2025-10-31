package pl.lib.automation;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
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
        JasperPrint tocPagePrint = createTocPage(mainContentPrint, config);

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

    private JasperPrint createTocPage(JasperPrint mainContentPrint, ReportConfig config) throws JRException {
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
        titleBand.addElement(createStaticText("Spis Treści", 0, 10, design.getColumnWidth(), 30, 22, true, HorizontalTextAlignEnum.CENTER));
        design.setTitle(titleBand);

        List<net.sf.jasperreports.engine.PrintBookmark> allBookmarks = new ArrayList<>();
        collectAllBookmarks(mainContentPrint.getBookmarks(), allBookmarks);

        int yPos = 20;
        int maxTocItems = Math.min(allBookmarks.size(), 30);
        int requiredHeight = 100;

        if (!allBookmarks.isEmpty()) {
            requiredHeight = yPos + (maxTocItems * 18) + 20;
        }

        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(requiredHeight);

        JRDesignSection detailSection = (JRDesignSection) design.getDetailSection();
        detailSection.addBand(detailBand);

        if (!allBookmarks.isEmpty()) {
            for (int i = 0; i < maxTocItems; i++) {
                net.sf.jasperreports.engine.PrintBookmark bookmark = allBookmarks.get(i);

                int bookmarkLevel = calculateBookmarkLevel(bookmark, mainContentPrint.getBookmarks());

                JRDesignStaticText labelText = new JRDesignStaticText();
                labelText.setX(20 + (bookmarkLevel * 15));
                labelText.setY(yPos);
                labelText.setWidth(design.getColumnWidth() - 100 - (bookmarkLevel * 15));
                labelText.setHeight(15);
                labelText.setText(bookmark.getLabel());
                labelText.setFontName("DejaVu Sans");
                labelText.setFontSize(10f - (bookmarkLevel * 0.5f));
                labelText.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);

                detailBand.addElement(labelText);

                JRDesignStaticText pageText = new JRDesignStaticText();
                pageText.setX(design.getColumnWidth() - 60);
                pageText.setY(yPos);
                pageText.setWidth(60);
                pageText.setHeight(15);
                pageText.setText(String.valueOf(bookmark.getPageIndex() + 1));
                pageText.setFontName("DejaVu Sans");
                pageText.setFontSize(10f);
                pageText.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);

                detailBand.addElement(pageText);

                yPos += 18;
            }
        } else {
            detailBand.addElement(createStaticText(
                "Brak zakładek w raporcie głównym.",
                0, 50, design.getColumnWidth(), 20, 10, false, HorizontalTextAlignEnum.CENTER
            ));
        }

        JasperReport report = builder.build();
        return JasperFillManager.fillReport(report, new HashMap<>(), new JREmptyDataSource());
    }

    private List<net.sf.jasperreports.engine.PrintBookmark> collectAllBookmarks(
            List<net.sf.jasperreports.engine.PrintBookmark> bookmarks,
            List<net.sf.jasperreports.engine.PrintBookmark> result) {

        if (bookmarks == null) {
            return result;
        }

        for (net.sf.jasperreports.engine.PrintBookmark bookmark : bookmarks) {
            result.add(bookmark);
            if (bookmark.getBookmarks() != null && !bookmark.getBookmarks().isEmpty()) {
                collectAllBookmarks(bookmark.getBookmarks(), result);
            }
        }

        return result;
    }

    private int calculateBookmarkLevel(net.sf.jasperreports.engine.PrintBookmark target,
                                       List<net.sf.jasperreports.engine.PrintBookmark> rootBookmarks) {
        return calculateBookmarkLevelRecursive(target, rootBookmarks, 0);
    }

    private int calculateBookmarkLevelRecursive(net.sf.jasperreports.engine.PrintBookmark target,
                                                List<net.sf.jasperreports.engine.PrintBookmark> bookmarks,
                                                int currentLevel) {
        if (bookmarks == null) {
            return -1;
        }

        for (net.sf.jasperreports.engine.PrintBookmark bookmark : bookmarks) {
            if (bookmark == target) {
                return currentLevel;
            }

            if (bookmark.getBookmarks() != null) {
                int foundLevel = calculateBookmarkLevelRecursive(target, bookmark.getBookmarks(), currentLevel + 1);
                if (foundLevel >= 0) {
                    return foundLevel;
                }
            }
        }

        return -1;
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