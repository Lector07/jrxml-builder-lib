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

                    // === KROK 1: Zdefiniuj WSZYSTKIE kolumny, które chcesz widzieć ===
                    // To przełączy generator w tryb "selektywny" i da Ci pełną kontrolę
                    .addColumn(ColumnDefinition.builder("chapterSegment").header("Rozdział").build())
                    .addColumn(ColumnDefinition.builder("paragraphSegment").header("Paragraf").build())
                    .addColumn(ColumnDefinition.builder("origin").visible(false).build())
                    .addColumn(ColumnDefinition.builder("financingSegment").visible(false).build())
                    .addColumn(ColumnDefinition.builder("paragraphGroup").visible(false).build())


                    //.addColumn(ColumnDefinition.builder("financingSegment").header("Finansowanie").width(60).build())
                    //.addColumn(ColumnDefinition.builder("origin").header("Źródło").width(60).build())
                    .addColumn(ColumnDefinition.builder("classificationName").header("Nazwa").build())
                    .addColumn(ColumnDefinition.builder("unitName").header("Jednostka").build())

                    // === KROK 2: Włącz sumowanie i ustaw stałą szerokość dla kolumn numerycznych ===
                    .addColumn(ColumnDefinition.builder("budgetAmount").header("Budżet")
                            // <-- Nadaj stałą szerokość
                            .format("#,##0.00")
                            .groupCalculation(Calculation.SUM)
                            .build())
                    .addColumn(ColumnDefinition.builder("increaseAmount").header("Zwiększenie")
                    // <-- Nadaj stałą szerokość
                            .format("#,##0.00")
                            .groupCalculation(Calculation.SUM)
                            .build())
                    .addColumn(ColumnDefinition.builder("reductionAmount").header("Zmniejszenie")
                           // <-- Nadaj stałą szerokość
                            .format("#,##0.00")
                            .groupCalculation(Calculation.SUM)
                            .build())
                    .addColumn(ColumnDefinition.builder("planAmount").header("Plan")
                           // <-- Nadaj stałą szerokość
                            .format("#,##0.00")
                            .groupCalculation(Calculation.SUM)
                            .build())

                    // Kolumna dla subraportu (nie dodajemy jej do kolumn głównych, bo jest w detail)
                    // .addColumn(ColumnDefinition.builder("resolutionElements").header("Szczegóły").build())


                    // === KROK 3: Włącz stopkę na POZIOMIE GRUPY ===
                    .addGroup(GroupDefinition.builder("paragraphGroup").showFooter(true).build())
                    .addGroup(GroupDefinition.builder("origin").showFooter(true).build())
                    .addGroup(GroupDefinition.builder("sectionSegment")
                            .label("\"Dział: \" + $F{sectionSegment}")
                            .showFooter(true) // Włącz stopkę z podsumowaniem
                            .build())
                    .addGroup(GroupDefinition.builder("chapterSegment").showFooter(true).build())
                    .addGroup(GroupDefinition.builder("classificationSymbol").showFooter(true).build())
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