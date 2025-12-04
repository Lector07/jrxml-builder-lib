package pl.lib.automation;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;
import pl.lib.automation.util.CurrencyFormatter;
import pl.lib.config.BudgetTableConfig;
import pl.lib.model.BudgetHierarchyNode;
import pl.lib.model.BudgetNodeType;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BudgetReportIntegrationTest {

    @Test
    void shouldGenerateBudgetReportWithHierarchy() throws JRException, IOException {
        String budgetJson = """
            {
              "metadata": {
                "title": "Sprawozdanie z wykonania budżetu 2024",
                "institution": "Gmina Miasto Chełm"
              },
              
              "1_wprowadzenie": {
                "opis": "Niniejsze sprawozdanie przedstawia wykonanie budżetu Gminy Miasto Chełm za rok 2024."
              },
              
              "2_dochody": {
                "podsumowanie": {
                  "plan": 125000000.00,
                  "wykonanie": 118500000.00
                },
                "struktura": [
                  {
                    "kod": "010",
                    "nazwa": "Rolnictwo i łowiectwo",
                    "level": 1,
                    "plan": 500000.00,
                    "wykonanie": 475000.00,
                    "dzieci": [
                      {
                        "kod": "01010",
                        "nazwa": "Infrastruktura wodociągowa i sanitacyjna wsi",
                        "level": 2,
                        "plan": 300000.00,
                        "wykonanie": 285000.00
                      },
                      {
                        "kod": "01095",
                        "nazwa": "Pozostała działalność",
                        "level": 2,
                        "plan": 200000.00,
                        "wykonanie": 190000.00
                      }
                    ]
                  },
                  {
                    "kod": "750",
                    "nazwa": "Administracja publiczna",
                    "level": 1,
                    "plan": 8500000.00,
                    "wykonanie": 8200000.00,
                    "dzieci": [
                      {
                        "kod": "75011",
                        "nazwa": "Urzędy wojewódzkie",
                        "level": 2,
                        "plan": 5000000.00,
                        "wykonanie": 4850000.00
                      },
                      {
                        "kod": "75023",
                        "nazwa": "Urzędy gmin",
                        "level": 2,
                        "plan": 3500000.00,
                        "wykonanie": 3350000.00
                      }
                    ]
                  }
                ]
              },
              
              "3_wydatki": {
                "struktura": [
                  {
                    "kod": "010",
                    "nazwa": "Rolnictwo i łowiectwo",
                    "level": 1,
                    "plan": 450000.00,
                    "wykonanie": 430000.00
                  },
                  {
                    "kod": "750",
                    "nazwa": "Administracja publiczna",
                    "level": 1,
                    "plan": 9000000.00,
                    "wykonanie": 8750000.00
                  }
                ]
              }
            }
            """;

        JsonReportGenerator generator = new JsonReportGenerator();
        JasperPrint print = generator.generateReport(budgetJson, "Sprawozdanie Budżetowe 2024", "Chełm", true);

        assertNotNull(print);
        assertTrue(print.getPages().size() > 0);

        File outputDir = new File("target/test-output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File("target/test-output/raport_budzetowy_test.pdf");
        JasperExportManager.exportReportToPdfFile(print, outputFile.getAbsolutePath());

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        System.out.println("✓ Raport budżetowy wygenerowany: " + outputFile.getAbsolutePath());
    }

    @Test
    void shouldFormatCurrencyCorrectly() {
        CurrencyFormatter formatter = CurrencyFormatter.forPLN();

        String formatted1 = formatter.formatAmount(new BigDecimal("1234567.89"));
        assertEquals("1 234 567,89 zł", formatted1);

        String formatted2 = formatter.calculateAndFormatExecutionPercent(
            new BigDecimal("1000000"),
            new BigDecimal("948000")
        );
        assertEquals("94,80%", formatted2);

        System.out.println("✓ Formatowanie walut działa poprawnie");
    }

    @Test
    void shouldBuildBudgetHierarchy() {
        BudgetHierarchyNode root = new BudgetHierarchyNode("750", "Administracja publiczna", BudgetNodeType.SECTION, 1);
        root.setPlannedAmount(new BigDecimal("9000000"));
        root.setActualAmount(new BigDecimal("8750000"));

        BudgetHierarchyNode child1 = new BudgetHierarchyNode("75011", "Urzędy wojewódzkie", BudgetNodeType.CHAPTER, 2);
        child1.setPlannedAmount(new BigDecimal("5000000"));
        child1.setActualAmount(new BigDecimal("4850000"));

        BudgetHierarchyNode child2 = new BudgetHierarchyNode("75023", "Urzędy gmin", BudgetNodeType.CHAPTER, 2);
        child2.setPlannedAmount(new BigDecimal("3500000"));
        child2.setActualAmount(new BigDecimal("3350000"));

        root.addChild(child1);
        root.addChild(child2);

        assertEquals(new BigDecimal("17500000"), root.getTotalPlanned());
        assertEquals(new BigDecimal("16950000"), root.getTotalActual());

        BigDecimal totalExecutionPercent = root.getTotalActual()
            .divide(root.getTotalPlanned(), 4, java.math.RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, java.math.RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("96.86"), totalExecutionPercent);

        System.out.println("✓ Hierarchia budżetowa budowana prawidłowo");
        System.out.println("  - Plan całkowity: " + root.getTotalPlanned());
        System.out.println("  - Wykonanie całkowite: " + root.getTotalActual());
        System.out.println("  - % wykonania: " + root.getExecutionPercent() + "%");
    }

    @Test
    void shouldGenerateSimpleBudgetTable() throws JRException, IOException {
        String simpleBudget = """
            {
              "budzet_2024": {
                "struktura": [
                  {
                    "kod": "750",
                    "nazwa": "Administracja publiczna",
                    "level": 1,
                    "plan": 9000000.00,
                    "wykonanie": 8750000.00
                  },
                  {
                    "kod": "801",
                    "nazwa": "Oświata i wychowanie",
                    "level": 1,
                    "plan": 48000000.00,
                    "wykonanie": 46500000.00
                  },
                  {
                    "kod": "852",
                    "nazwa": "Pomoc społeczna",
                    "level": 1,
                    "plan": 26000000.00,
                    "wykonanie": 24800000.00
                  }
                ]
              }
            }
            """;

        JsonReportGenerator generator = new JsonReportGenerator();
        JasperPrint print = generator.generateReport(simpleBudget, "Test Prostej Tabeli Budżetowej", "Warszawa", false);

        assertNotNull(print);

        File outputDir = new File("target/test-output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File("target/test-output/raport_prosty_budzet.pdf");
        JasperExportManager.exportReportToPdfFile(print, outputFile.getAbsolutePath());

        assertTrue(outputFile.exists());
        System.out.println("✓ Prosty raport budżetowy: " + outputFile.getAbsolutePath());
    }

    @Test
    void shouldHandleDeepHierarchy() {
        BudgetHierarchyNode section = new BudgetHierarchyNode("750", "Administracja publiczna", BudgetNodeType.SECTION, 1);
        section.setPlannedAmount(BigDecimal.ZERO);
        section.setActualAmount(BigDecimal.ZERO);

        BudgetHierarchyNode chapter = new BudgetHierarchyNode("75011", "Urzędy wojewódzkie", BudgetNodeType.CHAPTER, 2);
        chapter.setPlannedAmount(BigDecimal.ZERO);
        chapter.setActualAmount(BigDecimal.ZERO);

        BudgetHierarchyNode para1 = new BudgetHierarchyNode("4010", "Wynagrodzenia osobowe", BudgetNodeType.PARAGRAPH, 3);
        para1.setPlannedAmount(new BigDecimal("3000000"));
        para1.setActualAmount(new BigDecimal("2950000"));

        BudgetHierarchyNode para2 = new BudgetHierarchyNode("4040", "Dodatkowe wynagrodzenia", BudgetNodeType.PARAGRAPH, 3);
        para2.setPlannedAmount(new BigDecimal("500000"));
        para2.setActualAmount(new BigDecimal("485000"));

        BudgetHierarchyNode para3 = new BudgetHierarchyNode("4110", "Składki na ubezpieczenie społeczne", BudgetNodeType.PARAGRAPH, 3);
        para3.setPlannedAmount(new BigDecimal("600000"));
        para3.setActualAmount(new BigDecimal("590000"));

        chapter.addChild(para1);
        chapter.addChild(para2);
        chapter.addChild(para3);
        section.addChild(chapter);

        assertEquals(new BigDecimal("4100000"), section.getTotalPlanned());
        assertEquals(new BigDecimal("4025000"), section.getTotalActual());

        assertTrue(section.hasChildren());
        assertTrue(chapter.hasChildren());
        assertFalse(para1.hasChildren());

        System.out.println("✓ Głęboka hierarchia (3 poziomy) działa poprawnie");
        System.out.println("  - Dział: " + section.getCode());
        System.out.println("  - Rozdział: " + chapter.getCode());
        System.out.println("  - Paragrafy: " + chapter.getChildren().size());
    }

    @Test
    void shouldCalculateExecutionPercentages() {
        CurrencyFormatter formatter = new CurrencyFormatter();

        BigDecimal plan1 = new BigDecimal("1000000");
        BigDecimal actual1 = new BigDecimal("948000");
        String percent1 = formatter.calculateAndFormatExecutionPercent(plan1, actual1);
        assertEquals("94,80%", percent1);

        BigDecimal plan2 = new BigDecimal("5000000");
        BigDecimal actual2 = new BigDecimal("5125000");
        String percent2 = formatter.calculateAndFormatExecutionPercent(plan2, actual2);
        assertEquals("102,50%", percent2);

        BigDecimal plan3 = new BigDecimal("2000000");
        BigDecimal actual3 = new BigDecimal("1500000");
        String percent3 = formatter.calculateAndFormatExecutionPercent(plan3, actual3);
        assertEquals("75,00%", percent3);

        System.out.println("✓ Wszystkie obliczenia % wykonania są prawidłowe");
    }
}

