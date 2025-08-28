package pl.lib.automation;

import pl.lib.automation.AutomatedReportService;
import pl.lib.config.ColumnDefinition;
import pl.lib.config.GroupDefinition;
import pl.lib.config.ReportConfig;
import pl.lib.model.Calculation;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonReportExample {
    public static void main(String[] args) {
        AutomatedReportService service = new AutomatedReportService();
        String filePath = "RESOLUTION-CHANGES.json";

        try {
            System.out.println("Reading data from file: " + filePath);
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));

            ReportConfig config = new ReportConfig.Builder()
                    .title("Plans")

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