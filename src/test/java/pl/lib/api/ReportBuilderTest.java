// java
package pl.lib.api;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.CalculationEnum;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ResetTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.lib.model.*;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportBuilderTest {

    private ReportBuilder reportBuilder;

    @BeforeEach
    void setUp() {
        reportBuilder = new ReportBuilder("TestReport");
    }

    @Test
    void testBuild_SuccessfulCompilation() {
        assertDoesNotThrow(() -> {
            reportBuilder.addColumn(new Column("name", "Name", 100, DataType.STRING, null, null, null));
            reportBuilder.build();
        }, "Building a simple report should not throw an exception.");
    }

    @Test
    void testWithTitle_SetsReportTitleParameter() {
        String title = "My Awesome Report";
        reportBuilder.withTitle(title);
        Map<String, Object> params = reportBuilder.getParameters();
        assertEquals(title, params.get("ReportTitle"), "The ReportTitle parameter should be set correctly.");
    }

    @Test
    void testAddColumn_DeclaresFieldCorrectly() throws JRException {
        reportBuilder.addColumn(new Column("id", "ID", 50, DataType.INTEGER, null, null, null));
        JasperReport report = reportBuilder.build();
        JRField field = findField(report, "id");
        assertNotNull(field, "Field 'id' should be declared in the report.");
        assertEquals(Integer.class, field.getValueClass(), "Field 'id' should have the correct class type.");
    }

    @Test
    void testAddColumn_HandlesDuplicateFieldsGracefully() {
        reportBuilder.addColumn(new Column("name", "Name", 100, DataType.STRING, null, null, null));
        reportBuilder.addColumn(new Column("name", "Duplicate Name", 150, DataType.STRING, null, null, null));

        assertDoesNotThrow(() -> {
            JasperReport report = reportBuilder.build();
            assertEquals(1, report.getFields().length, "Only one field named 'name' should be declared.");
        }, "Building a report with duplicate column field names should not throw an exception.");
    }

    @Test
    void testAddGroup_DeclaresGroupAndField() throws JRException {
        // Język raportu to Java, więc składamy napis operatorem + i używamy cudzysłowów.
        reportBuilder.addGroup(new Group("category", "\"Category: \" + $F{category}", null, false, true));
        JasperReport report = reportBuilder.build();

        assertNotNull(findGroup(report, "Group_category"), "Group 'Group_category' should be created.");
        assertNotNull(findField(report, "category"), "Field for the group 'category' should be declared.");
    }

    @Test
    void testAddStyle_CreatesAndAddsStyle() throws JRException {
        Style testStyle = new Style("TestStyle")
                .withFont("Arial", 12, true)
                .withColors("#FFFFFF", "#000000")
                .withAlignment("Center", "Middle");

        reportBuilder.addStyle(testStyle);
        JasperReport report = reportBuilder.build();

        JRStyle style = findStyle(report, "TestStyle");
        assertNotNull(style, "Style 'TestStyle' should be added to the report.");
        assertEquals("Arial", style.getFontName());
        assertEquals(Float.valueOf(12f), style.getFontsize());
        assertTrue(style.isBold());
        assertEquals(HorizontalTextAlignEnum.CENTER, style.getHorizontalTextAlign());
    }

    @Test
    void testCalculateColumnWidths_DistributesAutoWidthCorrectly() throws Exception {
        reportBuilder.withPageSize(600, 800).withMargins(50, 50, 50, 50); // Available width = 500
        reportBuilder.addColumn(new Column("id", "ID", 100, DataType.INTEGER, null, null, null));
        reportBuilder.addColumn(new Column("name", "Name", -1, DataType.STRING, null, null, null)); // auto
        reportBuilder.addColumn(new Column("value", "Value", -1, DataType.STRING, null, null, null)); // auto

        JasperReport report = reportBuilder.build();

        // Access the private 'columns' field via reflection to check the calculated width
        Field columnsField = ReportBuilder.class.getDeclaredField("columns");
        columnsField.setAccessible(true);
        java.util.List<Column> columns = (java.util.List<Column>) columnsField.get(reportBuilder);

        Column nameColumn = columns.stream().filter(c -> c.getFieldName().equals("name")).findFirst().orElse(null);
        Column valueColumn = columns.stream().filter(c -> c.getFieldName().equals("value")).findFirst().orElse(null);

        assertNotNull(nameColumn);
        assertNotNull(valueColumn);

        // 500 (available) - 100 (fixed) = 400. 400 / 2 (auto columns) = 200
        assertEquals(200, nameColumn.getWidth(), "Auto-width for 'name' column should be calculated correctly.");
        assertEquals(200, valueColumn.getWidth(), "Auto-width for 'value' column should be calculated correctly.");
    }

    @Test
    void testBuildDetailBand_SetsCorrectWidthForFields() throws JRException {
        reportBuilder.withPageSize(500, 800).withMargins(0, 0, 0, 0);
        reportBuilder.addColumn(new Column("col1", "Column 1", 150, DataType.STRING, null, null, null));
        reportBuilder.addColumn(new Column("col2", "Column 2", 250, DataType.STRING, null, null, null));

        JasperReport report = reportBuilder.build();
        JRBand detailBand = report.getDetailSection().getBands()[0];

        JRElement[] elements = detailBand.getElements();
        assertEquals(150, elements[0].getWidth(), "Width of the first field in detail band should match column width.");
        assertEquals(250, elements[1].getWidth(), "Width of the second field in detail band should match column width.");
    }

    @Test
    void testDeclareVariables_CreatesSumVariables() throws JRException {
        reportBuilder.addColumn(new Column("amount", "Amount", 100, DataType.BIG_DECIMAL, null, Calculation.SUM, Calculation.SUM));
        // Używamy stałego napisu jako prawidłowego wyrażenia Java.
        reportBuilder.addGroup(new Group("category", "\"Category\"", null, true, true));

        JasperReport report = reportBuilder.build();

        // Report-level variable
        String reportVarName = "amount_REPORT_SUM";
        JRVariable reportVar = findVariable(report, reportVarName);
        assertNotNull(reportVar, "Report-level sum variable should be created.");
        assertEquals(CalculationEnum.SUM, reportVar.getCalculationValue());
        assertEquals(ResetTypeEnum.REPORT, reportVar.getResetTypeValue());

        // Group-level variable
        String groupVarName = "amount_Group_category_SUM";
        JRVariable groupVar = findVariable(report, groupVarName);
        assertNotNull(groupVar, "Group-level sum variable should be created.");
        assertEquals(CalculationEnum.SUM, groupVar.getCalculationValue());
        assertEquals(ResetTypeEnum.GROUP, groupVar.getResetTypeValue());
        assertEquals("Group_category", groupVar.getResetGroup().getName());
    }

    @Test
    void testBuild_WithSubreport_DeclaresParameters() throws JRException {
        String subreportParamName = "SUB_OBJECT_mySub";
        // Unikamy odwołania do niezadeklarowanego pola; używamy pustego datasource.
        Subreport subreport = new Subreport("DETAIL", null, "new net.sf.jasperreports.engine.JREmptyDataSource()")
                .withSubreportObjectParameterName(subreportParamName);
        reportBuilder.addSubreport(subreport);

        JasperReport report = reportBuilder.build();

        JRParameter subreportParam = findParameter(report, subreportParamName);
        assertNotNull(subreportParam, "Subreport object parameter should be declared.");
        assertEquals(JasperReport.class, subreportParam.getValueClass(), "Subreport parameter should be of type JasperReport.");
    }

    // --- Helpers (JasperReports nie udostępnia tu map) ---

    private JRField findField(JasperReport report, String name) {
        for (JRField f : report.getFields()) {
            if (name.equals(f.getName())) return f;
        }
        return null;
    }

    private JRGroup findGroup(JasperReport report, String name) {
        for (JRGroup g : report.getGroups()) {
            if (name.equals(g.getName())) return g;
        }
        return null;
    }

    private JRStyle findStyle(JasperReport report, String name) {
        JRStyle[] styles = report.getStyles();
        if (styles == null) return null;
        for (JRStyle s : styles) {
            if (name.equals(s.getName())) return s;
        }
        return null;
    }

    private JRVariable findVariable(JasperReport report, String name) {
        for (JRVariable v : report.getVariables()) {
            if (name.equals(v.getName())) return v;
        }
        return null;
    }

    private JRParameter findParameter(JasperReport report, String name) {
        for (JRParameter p : report.getParameters()) {
            if (name.equals(p.getName())) return p;
        }
        return null;
    }
}