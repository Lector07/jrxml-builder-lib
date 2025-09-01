package pl.lib.automation;

import pl.lib.automation.AutomatedReportService;
import pl.lib.config.ColumnDefinition;
import pl.lib.config.GroupDefinition;
import pl.lib.config.ReportConfig;
import pl.lib.model.Calculation;
import pl.lib.model.CompanyInfo;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonReportExample {
    public static void main(String[] args) {
        AutomatedReportService service = new AutomatedReportService();
        String filePath = "RESOLUTION-CHANGES.json";

        try {
            System.out.println("Reading data from file: " + filePath);
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));

            CompanyInfo companyInfo = CompanyInfo.builder("BIURO USŁUG KOMPUTEROWYCH \"SOFTRES\" SP Z O.O")
                    .address("ul. Zaciszna 44")
                    .location("35-326", "Rzeszów")
                    .taxId("8130335217")
                    .build();

            ReportConfig config = new ReportConfig.Builder()
                    .title("Plans")

                    .addColumn(ColumnDefinition.builder("chapterSegment").header("Rozdział").build())
                    .addColumn(ColumnDefinition.builder("paragraphSegment").header("Paragraf").visible(false).build())
                    .addColumn(ColumnDefinition.builder("origin").visible(false).build())
                    .addColumn(ColumnDefinition.builder("financingSegment").visible(false).build())
                    .addColumn(ColumnDefinition.builder("paragraphGroup").visible(false).build())


                    .addColumn(ColumnDefinition.builder("classificationName").header("Nazwa").build())
                    .addColumn(ColumnDefinition.builder("unitName").header("Jednostka").build())

                    .addColumn(ColumnDefinition.builder("budgetAmount").header("Budżet")
                            .format("#,##0.00")
                            .groupCalculation(Calculation.SUM)
                            .build())
                    .addColumn(ColumnDefinition.builder("increaseAmount").header("Zwiększenie")
                            .format("#,##0.00")
                            .groupCalculation(Calculation.SUM)
                            .build())
                    .addColumn(ColumnDefinition.builder("reductionAmount").header("Zmniejszenie")
                            .format("#,##0.00").visible(true).reportCalculation(Calculation.COUNT)
                            .groupCalculation(Calculation.DISTINCT_COUNT)
                            .build())
                    .addColumn(ColumnDefinition.builder("planAmount").header("Plan")
                            .format("#,##0.00")
                            .groupCalculation(Calculation.SUM)
                            .build())

                    .addGroup(GroupDefinition.builder("paragraphGroup").showFooter(true).build())
                    .addGroup(GroupDefinition.builder("origin").showFooter(true).build())
                    .addGroup(GroupDefinition.builder("sectionSegment")
                            .label("\"Dział: \" + $F{sectionSegment}")
                            .showFooter(true)
                            .build())
                    .addGroup(GroupDefinition.builder("chapterSegment").showFooter(true).build())
                    .companyInfo(companyInfo)
                    .build();

            System.out.println("Generating automatic report with JRXML in console...");
            service.generatePdfReportFromJsonWithJrxml(jsonData, config, "zmiany_w_uchwalach.pdf");

            System.out.println("\n Report has been generated successfully!");
            System.out.println(" PDF file saved as: zmiany_w_uchwalach.pdf");

        } catch (Exception e) {
            System.err.println("Error during report generation:");
            e.printStackTrace();
        }
    }
}