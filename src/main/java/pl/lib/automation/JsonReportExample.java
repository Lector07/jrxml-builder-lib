package pl.lib.automation;

import pl.lib.automation.AutomatedReportService;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonReportExample {
    public static void main(String[] args) {
        AutomatedReportService service = new AutomatedReportService();
        // File name in the project's root directory
        String filePath = "RESOLUTION-CHANGES.json";

        try {
            System.out.println("Loading data from file: " + filePath);
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));

            System.out.println("Generating automatic report with JRXML in console...");
            service.generatePdfReportFromJsonWithJrxml(jsonData, "Summary of Changes in Resolutions", "resolution_changes.pdf");

            System.out.println("\nReport generated successfully!");
            System.out.println("PDF file saved as: resolution_changes.pdf");

        } catch (Exception e) {
            System.err.println("❌ Error during report generation:");
            e.printStackTrace();
        }
    }
}