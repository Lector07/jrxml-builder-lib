package pl.lib.api;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.VerticalAlignEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.lib.model.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ReportBuilderTest {

    private ReportBuilder reportBuilder;

    @BeforeEach
    void setUp() {
        reportBuilder = new ReportBuilder();
    }

    @Test
    void testBasicReportGeneration() throws JRException {
        JasperReport jasperReport = reportBuilder
                .withTitle("Testowy Raport")
                .withPageSize(842, 595) // A4 Poziomo
                .build();

        assertNotNull(jasperReport);
        assertEquals(842, jasperReport.getPageWidth());
        assertEquals(595, jasperReport.getPageHeight());
        assertNotNull(jasperReport.getTitle());
        assertTrue(jasperReport.getTitle().getHeight() > 0);
        assertNotNull(Arrays.stream(jasperReport.getParameters()).anyMatch(p -> p.getName().equals("ReportTitle")));
    }

    @Test
    void testHorizontalLayout() throws JRException {
        JasperReport jasperReport = reportBuilder
                .withPageSize(595, 842) // A4 Portret
                .withHorizontalLayout()
                .build();

        assertEquals(842, jasperReport.getPageWidth());
        assertEquals(595, jasperReport.getPageHeight());
    }

    @Test
    void testColumnAdditionAndFieldGeneration() throws JRException {
        JasperReport jasperReport = reportBuilder
                .addColumn(new Column("USER_NAME", "Nazwa użytkownika", 150, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("ORDER_COUNT", "Liczba zamówień", 100, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE))
                .build();

        Map<String, JRField> fieldsMap = Arrays.stream(jasperReport.getFields()).collect(Collectors.toMap(JRField::getName, f -> f));

        assertTrue(fieldsMap.containsKey("USER_NAME"));
        assertEquals("java.lang.String", fieldsMap.get("USER_NAME").getValueClassName());

        assertTrue(fieldsMap.containsKey("ORDER_COUNT"));
        assertEquals("java.lang.Integer", fieldsMap.get("ORDER_COUNT").getValueClassName());

        assertNotNull(jasperReport.getColumnHeader());
        // Sprawdzamy, czy są 2 elementy w nagłówku (dla 2 kolumn)
        assertEquals(2, jasperReport.getColumnHeader().getElements().length);
    }

    @Test
    void testAutoWidthColumnCalculation() throws JRException {
        // Szerokość strony: 595, marginesy: 20 + 20 = 40. Dostępna szerokość: 555.
        // Kolumna stała: 155. Zostaje: 400. Dwie kolumny auto => 200px na każdą.
        JasperReport jasperReport = reportBuilder
                .addColumn(new Column("ID", "ID", -1, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("FIXED_COL", "Stała", 155, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("DESC", "Opis", -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .build();

        JRElement[] headerElements = jasperReport.getColumnHeader().getElements();
        assertEquals(3, headerElements.length);
        assertEquals(200, headerElements[0].getWidth());
        assertEquals(155, headerElements[1].getWidth());
        assertEquals(200, headerElements[2].getWidth());
    }

    @Test
    void testZebraStriping() throws JRException {
        JasperReport jasperReport = reportBuilder
                .addColumn(new Column("TEST", "Test", 100, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .build();

        Map<String, JRStyle> stylesMap = Arrays.stream(jasperReport.getStyles()).collect(Collectors.toMap(JRStyle::getName, s -> s));
        JRStyle zebraStyle = stylesMap.get("ZebraStripeStyle");
        assertNotNull(zebraStyle);
        assertEquals(Color.decode("#F0F0F0"), zebraStyle.getBackcolor());
    }

    @Test
    void testCustomStyle() throws JRException {
        Style customStyle = new Style("Highlight")
                .withColors("#FF0000", "#FFFF00")
                .withFont("Arial", 12, true);

        JasperReport jasperReport = reportBuilder
                .addStyle(customStyle)
                .addColumn(new Column("STYLED_COL", "Styl", 100, DataType.STRING, null, Calculation.NONE, Calculation.NONE, "Highlight"))
                .build();

        Map<String, JRStyle> stylesMap = Arrays.stream(jasperReport.getStyles()).collect(Collectors.toMap(JRStyle::getName, s -> s));
        JRStyle style = stylesMap.get("Highlight");
        assertNotNull(style);
        assertEquals("Arial", style.getFontName());
        assertTrue(style.isBold());
        assertEquals(Color.decode("#FF0000"), style.getForecolor());
        assertEquals(Color.decode("#FFFF00"), style.getBackcolor());
    }

    @Test
    void testGroupingAndFieldDeclaration() throws JRException {
        // Testujemy, czy pole do grupowania jest poprawnie deklarowane, nawet jeśli nie jest kolumną
        JasperReport jasperReport = reportBuilder
                .addGroup(new Group("CATEGORY_ID", "\"Kategoria: \" + $F{CATEGORY_ID}", "GroupHeaderStyle", false))
                .addColumn(new Column("PRODUCT", "Produkt", 200, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .build();

        // Sprawdzenie grupy
        assertEquals(1, jasperReport.getGroups().length);
        JRGroup group = jasperReport.getGroups()[0];
        assertEquals("Group_CATEGORY_ID", group.getName());
        assertEquals("$F{CATEGORY_ID}", group.getExpression().getText());

        // Sprawdzenie, czy pole dla grupy zostało zadeklarowane
        Map<String, JRField> fieldsMap = Arrays.stream(jasperReport.getFields()).collect(Collectors.toMap(JRField::getName, f -> f));
        JRField groupField = fieldsMap.get("CATEGORY_ID");
        assertNotNull(groupField);
        assertEquals("java.lang.String", groupField.getValueClassName()); // Domyślny typ dla pola grupy
    }

    @Test
    void testStandardFooter() throws JRException {
        JasperReport jasperReport = reportBuilder
                .withStandardFooter("Lewa stopka", "Prawa stopka")
                .build();

        assertNotNull(jasperReport.getPageFooter());
        // Weryfikacja jest trudna bez generowania raportu, ale sprawdzamy, czy sekcja istnieje
        assertTrue(jasperReport.getPageFooter().getHeight() > 0);
    }


    @Test
    void testHiddenColumn() throws JRException {
        // Kolumna z szerokością 0 nie powinna być widoczna w nagłówku ani w sekcji detail
        JasperReport jasperReport = reportBuilder
                .addColumn(new Column("ID", "ID", 0, DataType.INTEGER, null, Calculation.NONE, Calculation.NONE))
                .addColumn(new Column("NAME", "Nazwa", 200, DataType.STRING, null, Calculation.NONE, Calculation.NONE))
                .build();

        // Pole powinno być zadeklarowane
        Map<String, JRField> fieldsMap = Arrays.stream(jasperReport.getFields()).collect(Collectors.toMap(JRField::getName, f -> f));
        assertNotNull(fieldsMap.get("ID"));

        // Ale nie powinno być elementu w nagłówku ani w detail
        assertEquals(1, jasperReport.getColumnHeader().getElements().length);
        assertEquals(1, ((JRBand) jasperReport.getDetailSection().getBands()[0]).getElements().length);
    }
}