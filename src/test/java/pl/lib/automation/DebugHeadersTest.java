package pl.lib.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.junit.jupiter.api.Test;
import pl.lib.automation.compiler.SubreportCompiler;

/**
 * Test debugowania do sprawdzenia jakie nagłówki są generowane
 */
class DebugHeadersTest {

    @Test
    void shouldPrintGeneratedHeaders() throws Exception {
        String jsonTable = """
                [
                    {"user_name": "Jan", "email_address": "jan@test.com", "phone_number": "123"},
                    {"user_name": "Anna", "email_address": "anna@test.com", "phone_number": "456"}
                ]
                """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tableData = mapper.readTree(jsonTable);

        SubreportCompiler compiler = new SubreportCompiler();
        JasperReport report = compiler.compileTableSubreport(tableData, 555);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 DEBUG: Analiza wygenerowanych nagłówków tabeli");
        System.out.println("=".repeat(80));


        System.out.println("\n📋 Dane wejściowe JSON:");
        System.out.println(jsonTable);

        System.out.println("\n✅ Raport został skompilowany poprawnie");
        System.out.println("📊 Nazwa raportu: " + report.getName());
        System.out.println("📏 Szerokość kolumny: " + report.getColumnWidth());

        System.out.println("\n💡 Aby zobaczyć nagłówki, należy otworzyć wygenerowany PDF:");
        System.out.println("   target/test-output/raport_formatowanie_naglowkow.pdf");
        System.out.println("=".repeat(80) + "\n");
    }
}

