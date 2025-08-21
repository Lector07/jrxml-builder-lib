package pl.lib.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReportBuilderTest {

    @Test
    void shouldBuildReportWithCustomTitle() {
        ReportBuilder builder = new ReportBuilder();
        String expectedTitle = "Testowy Tytuł Raportu";

        String jrxml = builder.withTitle(expectedTitle).build();

        assertNotNull(jrxml);
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[\"" + expectedTitle + "\"]]></textFieldExpression>"));
        System.out.println(jrxml);
    }
}