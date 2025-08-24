package pl.lib.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.lib.api.ReportBuilder;
import pl.lib.model.*;

import static org.junit.jupiter.api.Assertions.*;

class ReportBuilderTest {

    private ReportBuilder reportBuilder;

    @BeforeEach
    void setUp() {
        reportBuilder = new ReportBuilder();
    }

    @Test
    void testBasicReportGeneration() {
        String jrxml = reportBuilder
                .withTitle("Testowy Raport")
                .withPageSize(842, 595) // A4 Poziomo
                .build();

        assertNotNull(jrxml);
        assertTrue(jrxml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(jrxml.contains("<jasperReport"));
        assertTrue(jrxml.contains("name=\"Testowy_Raport\""));
        assertTrue(jrxml.contains("pageWidth=\"842\""));
        assertTrue(jrxml.contains("pageHeight=\"595\""));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$P{ReportTitle}]]></textFieldExpression>"));
        assertTrue(jrxml.endsWith("</jasperReport>"));

        System.out.println(jrxml);
    }

    @Test
    void testHorizontalLayout() {
        String jrxml = reportBuilder
                .withPageSize(595, 842) // A4 Portret
                .withHorizontalLayout()
                .build();

        // Sprawdza, czy wymiary zostały zamienione
        assertTrue(jrxml.contains("pageWidth=\"842\""));
        assertTrue(jrxml.contains("pageHeight=\"595\""));
        System.out.println(jrxml);

    }

    @Test
    void testColumnAdditionAndFieldGeneration() {
        String jrxml = reportBuilder
                .addColumn(new Column("USER_NAME", "Nazwa użytkownika", 150, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("ORDER_COUNT", "Liczba zamówień", 100, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE))
                .build();

        // Sprawdzenie definicji pól
        assertTrue(jrxml.contains("<field name=\"USER_NAME\" class=\"java.lang.String\"/>"));
        assertTrue(jrxml.contains("<field name=\"ORDER_COUNT\" class=\"java.lang.Integer\"/>"));

        // Sprawdzenie nagłówków kolumn
        assertTrue(jrxml.contains("<text><![CDATA[Nazwa użytkownika]]></text>"));
        assertTrue(jrxml.contains("<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"0\" y=\"0\" width=\"150\" height=\"25\"/>"));
        assertTrue(jrxml.contains("<text><![CDATA[Liczba zamówień]]></text>"));
        assertTrue(jrxml.contains("<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"150\" y=\"0\" width=\"100\" height=\"25\"/>"));

        // Sprawdzenie pól w sekcji detail
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$F{USER_NAME}]]></textFieldExpression>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$F{ORDER_COUNT}]]></textFieldExpression>"));
        System.out.println(jrxml);

    }

    @Test
    void testAutoWidthColumnCalculation() {
        // Szerokość strony: 595, marginesy: 20 + 20 = 40. Dostępna szerokość: 555.
        // Kolumna stała: 155. Zostaje: 400. Dwie kolumny auto => 200px na każdą.
        String jrxml = reportBuilder
                .addColumn(new Column("ID", "ID", -1, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("FIXED_COL", "Stała", 155, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("DESC", "Opis", 0, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .build();

        assertTrue(jrxml.contains("<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"0\" y=\"0\" width=\"200\" height=\"25\"/>"));
        assertTrue(jrxml.contains("<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"200\" y=\"0\" width=\"155\" height=\"25\"/>"));
        assertTrue(jrxml.contains("<reportElement mode=\"Opaque\" backcolor=\"#DEDEDE\" x=\"355\" y=\"0\" width=\"200\" height=\"25\"/>"));

        System.out.println(jrxml);

    }

    @Test
    void testZebraStriping() {
        String jrxml = reportBuilder
                .withZebraStriping()
                .addColumn(new Column("TEST", "Test", 100, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .build();

        assertTrue(jrxml.contains("<style name=\"ZebraStripeStyle\""));
        assertTrue(jrxml.contains("<conditionalStyle>"));
        assertTrue(jrxml.contains("<conditionExpression><![CDATA[$V{REPORT_COUNT} % 2 == 0]]></conditionExpression>"));
        assertTrue(jrxml.contains("<reportElement style=\"ZebraStripeStyle\""));

        System.out.println(jrxml);

    }

    @Test
    void testCustomStyle() {
        Style customStyle = new Style("Highlight")
                .withColors("#FF0000", "#FFFF00")
                .withFontName("Arial", 12, true);

        String jrxml = reportBuilder
                .addStyle(customStyle)
                .addColumn(new Column("STYLED_COL", "Styl", 100, DataType.STRING, null, Calculation.NONE, Calculation.NONE, "Highlight"))
                .build();

        assertTrue(jrxml.contains("<style name=\"Highlight\" fontName=\"Arial\" fontSize=\"12\" isBold=\"true\" forecolor=\"#FF0000\" mode=\"Opaque\" backcolor=\"#FFFF00\">"));
        assertTrue(jrxml.contains("<reportElement style=\"Highlight\""));

        System.out.println(jrxml);

    }

    @Test
    void testGroupingAndGroupCalculations() {
        String jrxml = reportBuilder
                .addGroup(new Group("CATEGORY", "\"Kategoria: \" + $F{CATEGORY}"))
                .addColumn(new Column("PRODUCT", "Produkt", 200, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("PRICE", "Cena", 100, DataType.BIG_DECIMAL, "#,##0.00", Calculation.SUM, Calculation.AVERAGE))
                .build();

        // Sprawdzenie grupy
        assertTrue(jrxml.contains("<group name=\"CATEGORYGroup\">"));
        assertTrue(jrxml.contains("<groupExpression><![CDATA[$F{CATEGORY}]]></groupExpression>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[\"Kategoria: \" + $F{CATEGORY}]]></textFieldExpression>"));

        // Sprawdzenie zmiennej dla obliczeń w grupie
        assertTrue(jrxml.contains("<variable name=\"PRICE_GROUP_AVERAGE\" class=\"java.math.BigDecimal\" resetType=\"Group\" resetGroup=\"CATEGORYGroup\" calculation=\"Average\">"));

        // Sprawdzenie stopki grupy
        assertTrue(jrxml.contains("<groupFooter>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$V{PRICE_GROUP_AVERAGE}]]></textFieldExpression>"));

        System.out.println(jrxml);

    }

    @Test
    void testReportCalculations() {
        String jrxml = reportBuilder
                .addColumn(new Column("AMOUNT", "Wartość", 100, DataType.INTEGER, null, Calculation.SUM, Calculation.NONE))
                .build();

        // Sprawdzenie zmiennej dla obliczeń raportu
        assertTrue(jrxml.contains("<variable name=\"AMOUNT_REPORT_SUM\" class=\"java.lang.Integer\" calculation=\"Sum\">"));

        // Sprawdzenie sekcji summary
        assertTrue(jrxml.contains("<summary>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$V{AMOUNT_REPORT_SUM}]]></textFieldExpression>"));

        System.out.println(jrxml);

    }

    @Test
    void testStandardFooter() {
        String jrxml = reportBuilder
                .withStandardFooter("Lewa stopka", "Prawa stopka")
                .build();

        assertTrue(jrxml.contains("<pageFooter>"));
        assertTrue(jrxml.contains("<text><![CDATA[Lewa stopka]]></text>"));
        assertTrue(jrxml.contains("<text><![CDATA[Prawa stopka]]></text>"));
        // Sprawdzenie paginacji
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[\"Strona \" + $V{PAGE_NUMBER} + \" z \"]]></textFieldExpression>"));

        System.out.println(jrxml);

    }

    @Test
    void testImageInTitle() {
        String jrxml = reportBuilder
                .addImageInTitle(new Image("\"logo.png\"", 10, 10, 100, 40))
                .build();

        assertTrue(jrxml.contains("<title>"));
        assertTrue(jrxml.contains("<image>"));
        assertTrue(jrxml.contains("<reportElement x=\"10\" y=\"10\" width=\"100\" height=\"40\"/>"));
        assertTrue(jrxml.contains("<imageExpression><![CDATA[\"logo.png\"]]></imageExpression>"));


        System.out.println(jrxml);

    }

    // pomocnicza metoda do zliczania wystąpień fragmentu
    private int countOccurrences(String text, String fragment) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(fragment, idx)) != -1) {
            count++;
            idx += fragment.length();
        }
        return count;
    }

    @Test
    void testThrowsOnSummationForNonNumericType() {
        ReportBuilder builder = new ReportBuilder();
        assertThrows(IllegalArgumentException.class, () ->
                builder.addColumn("name", "Nazwa", 100, DataType.STRING, null, true)
        );
    }

    @Test
    void testNoSummaryWhenNoSummedColumns() {
        String jrxml = new ReportBuilder()
                .withTitle("Brak sum")
                .addColumn("name", "Nazwa", 200, DataType.STRING)
                .addColumn("qty", "Ilość", 100, DataType.INTEGER)
                .build();

        assertNotNull(jrxml);
        assertFalse(jrxml.contains("<summary>"));
        assertFalse(jrxml.contains("_SUM"));
    }

    @Test
    void testPageSizeAndColumnWidthApplied() {
        int w = 800, h = 1000;
        String jrxml = new ReportBuilder()
                .withPageSize(w, h)
                .addColumn("name", "Nazwa", 300, DataType.STRING)
                .build();

        assertTrue(jrxml.contains("pageWidth=\"" + w + "\""));
        assertTrue(jrxml.contains("pageHeight=\"" + h + "\""));
        assertTrue(jrxml.contains("columnWidth=\"" + (w - 40) + "\""));
        assertTrue(jrxml.contains("leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\""));
    }

    @Test
    void testQueryStringIsEmpty() {
        String jrxml = new ReportBuilder()
                .addColumn("name", "Nazwa", 200, DataType.STRING)
                .build();

        assertTrue(jrxml.contains("<queryString><![CDATA[]]></queryString>"));
    }

    @Test
    void testTitleParameterAndExpressionPresent() {
        String jrxml = new ReportBuilder()
                .withTitle("Tytuł")
                .addColumn("name", "Nazwa", 200, DataType.STRING)
                .build();

        assertTrue(jrxml.contains("<parameter name=\"ReportTitle\" class=\"java.lang.String\"/>"));
        assertTrue(jrxml.contains("<textFieldExpression><![CDATA[$P{ReportTitle}]]></textFieldExpression>"));
    }

    @Test
    void testRightAlignmentOnlyForNumericFieldsInDetail() {
        String jrxml = new ReportBuilder()
                .addColumn("name", "Nazwa", 200, DataType.STRING)
                .addColumn("qty", "Ilość", 100, DataType.INTEGER) // bez sumowania, więc brak <summary>
                .build();

        // Dokładnie jedno wyrównanie do prawej w szczegółach (dla pola liczbowego)
        assertEquals(1, countOccurrences(jrxml, "<textElement textAlignment=\"Right\"/>"));
    }

    @Test
    void testNoPatternAttributeWhenPatternNull() {
        String jrxml = new ReportBuilder()
                .addColumn("name", "Nazwa", 200, DataType.STRING) // brak wzorca
                .build();

        assertFalse(jrxml.contains("pattern="));
    }

    @Test
    void testGeneratesVariablesOnlyForSummedColumns() {
        String jrxml = new ReportBuilder()
                .addColumn("productName", "Produkt", 200, DataType.STRING, null, false)
                .addColumn("quantity", "Ilość", 100, DataType.INTEGER, "#,##0", true)
                .addColumn("totalValue", "Wartość", 150, DataType.BIG_DECIMAL, "#,##0.00 zł", true)
                .build();

        assertTrue(jrxml.contains("<variable name=\"quantity_SUM\""));
        assertTrue(jrxml.contains("<variable name=\"totalValue_SUM\""));
        assertFalse(jrxml.contains("<variable name=\"productName_SUM\""));
    }

    @Test
    void testZebraStripingDisabledDoesNotEmitStyle() {
        String jrxml = new ReportBuilder()
                .withTitle("Bez zebry")
                .addColumn("name", "Nazwa", 300, DataType.STRING)
                .build();

        assertFalse(jrxml.contains("<style name=\"ZebraStripeStyle\""));
        assertFalse(jrxml.contains("style=\"ZebraStripeStyle\""));
    }
}