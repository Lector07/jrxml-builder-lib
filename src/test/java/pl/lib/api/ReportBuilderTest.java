package pl.lib.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReportBuilderTest {

    @Test
    void testBuildsReportWithDynamicColumns() {

        ReportBuilder builder = new ReportBuilder();

        String jrxml = builder
                .withTitle("Lista Pracowników")
                .addColumn("firstName", "Imię", 150)
                .addColumn("lastName", "Nazwisko", 25)
                .addColumn("position", "Stanowisko", 20)
                .build();

        assertNotNull(jrxml);

        assertTrue(jrxml.contains("<field name=\"firstName\" class=\"java.lang.String\"/>"));

        assertTrue(jrxml.contains("<text><![CDATA[Imię]]></text>"));

        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$F{firstName}]]></textFieldExpression>"));

        System.out.println(jrxml);
    }
}