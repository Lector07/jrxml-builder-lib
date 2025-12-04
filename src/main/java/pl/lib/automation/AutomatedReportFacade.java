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
    public void testCompositeReport() throws Exception {
        String jsonContent = """
                {
                  "1. Wstęp": {
                    "Cel raportu": "Przedstawienie kompleksowej analizy wykonania budżetu miasta Gliwice za rok 2024.",
                    "Data wygenerowania": "2025-11-05",
                    "Zakres danych": "Dane finansowe od 1 stycznia 2024 do 31 grudnia 2024.",
                    "1.1. Podstawa prawna": {
                      "Dokument główny": "Uchwała Rady Miasta nr LII/1078/2023 z dnia 14 grudnia 2023 r.",
                      "Inne regulacje": "Ustawa o finansach publicznych z dnia 27 sierpnia 2009 r."
                    }
                  },
                  "2. Analiza wskaźnikowa": {
                    "Opis sekcji": "Prezentacja kluczowych wskaźników budżetowych obrazujących sytuację finansową miasta.",
                    "2.1. Główne wskaźniki budżetowe": [
                      {
                        "Lp.": 1,
                        "Wskaźnik": "Udział dochodów bieżących w dochodach ogółem (WB1)",
                        "Wartość wyliczona": "90,56%",
                        "Zmiana r/r": "+0,81 pp"
                      },
                      {
                        "Lp.": 2,
                        "Wskaźnik": "Udział dochodów własnych w dochodach ogółem (WB2)",
                        "Wartość wyliczona": "66,99%",
                        "Zmiana r/r": "+1,20 pp"
                      },
                      {
                        "Lp.": 3,
                        "Wskaźnik": "Udział nadwyżki operacyjnej w dochodach bieżących (WB3)",
                        "Wartość wyliczona": "13,36%",
                        "Zmiana r/r": "+4,50 pp"
                      }
                    ],
                    "Wnioski z analizy": "Wskaźniki wskazują na stabilną i poprawiającą się kondycję finansową miasta.",
                    "Wykres dochodów": {
                      "type": "pie",
                      "data": {
                        "Dochody własne": 66.99,
                        "Subwencje": 23.89,
                        "Dotacje celowe": 7.22,
                        "Środki z UE": 1.90
                      },
                      "title": "Struktura dochodów budżetu miasta"
                    },
                    "Wykres wydatków": {
                      "type": "bar",
                      "data": {
                        "Oświata": 35.2,
                        "Transport": 18.5,
                        "Kultura": 12.3,
                        "Administracja": 15.8,
                        "Gospodarka komunalna": 18.2
                      },
                      "title": "Wydatki według działów budżetowych (%)"
                    }
                  },
                  "3. Omówienie wykonania dochodów": {
                    "Dochody ogółem (wykonanie)": "1.944.885.474,95 zł",
                    "Plan po zmianach": "1.903.250.875,31 zł",
                    "Procent realizacji planu": "102,19%",
                    "3.1. Struktura dochodów według źródeł": {
                      "Kluczowy wniosek": "Największy udział w dochodach mają dochody własne.",
                      "Źródła dochodów (tabela)": [
                        {
                          "Źródło": "Dochody własne",
                          "Kwota (zł)": "1.302.865.390,67",
                          "Udział (%)": "66,99"
                        },
                        {
                          "Źródło": "Subwencje",
                          "Kwota (zł)": "464.651.524,00",
                          "Udział (%)": "23,89"
                        },
                        {
                          "Źródło": "Dotacje celowe z budżetu państwa",
                          "Kwota (zł)": "140.362.311,46",
                          "Udział (%)": "7,22"
                        },
                        {
                          "Źródło": "Środki z UE i inne",
                          "Kwota (zł)": "36.006.248,82",
                          "Udział (%)": "1,90"
                        }
                      ]
                    },
                    "Wykres trendu": {
                      "type": "line",
                      "data": {
                        "2020": 1650.5,
                        "2021": 1720.3,
                        "2022": 1810.2,
                        "2023": 1890.4,
                        "2024": 1944.9
                      },
                      "title": "Trend dochodów budżetu miasta (mln zł)"
                    }
                  },
                  "4. Podsumowanie": {
                    "Ocena końcowa": "Realizacja budżetu w roku 2024 przebiegła pomyślnie, z nadwyżką budżetową.",
                    "Rekomendacje na przyszłość": "Rekomenduje się dalsze działania w celu dywersyfikacji źródeł dochodów własnych."
                  }
                }
                """;
        ReportConfig config = new ReportConfig();
        config.setTitle("Sprawozdanie z Wykonania Budżetu Miasta Gliwice za 2024 r.");
        config.setPageFormat("A4");
        config.setOrientation("PORTRAIT");

        CompanyInfo companyInfo = CompanyInfo.builder("Urząd Miasta Gliwice")
                .address("ul. Zwycięstwa 21")
                .location("44-100", "Gliwice")
                .build();
        config.setCompanyInfo(companyInfo);

        pl.lib.config.FormattingOptions formattingOptions = new pl.lib.config.FormattingOptions();
        formattingOptions.setGenerateBookmarks(true);
        config.setFormattingOptions(formattingOptions);
        AutomatedReportFacade facade = new AutomatedReportFacade(true);
        byte[] pdfBytes = facade.generateCompositeReport(jsonContent, config);
        java.nio.file.Files.write(
                java.nio.file.Paths.get("raport_budzet_gliwice.pdf"),
                pdfBytes
        );
        System.out.println("Raport wygenerowany pomyślnie: raport_budzet_gliwice.pdf");
    }
}