package pl.lib.automation;

import pl.lib.automation.AutomatedReportService;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonReportExample {
    public static void main(String[] args) {
        AutomatedReportService service = new AutomatedReportService();
        String filePath = "realization.json"; // Nazwa pliku w głównym katalogu projektu

        try {
            System.out.println("Wczytywanie danych z pliku: " + filePath);
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));

            System.out.println("Generowanie automatycznego raportu z JRXML w konsoli...");
            service.generatePdfReportFromJsonWithJrxml(jsonData, "Zestawienie zmian w uchwałach", "zmiany_w_uchwalach.pdf");

            System.out.println("\n✅ Raport został wygenerowany pomyślnie!");
            System.out.println("📁 Plik PDF zapisany jako: zmiany_w_uchwalach.pdf");

        } catch (Exception e) {
            System.err.println("❌ Błąd podczas generowania raportu:");
            e.printStackTrace();
        }
    }
}