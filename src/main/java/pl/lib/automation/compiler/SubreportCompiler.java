package pl.lib.automation.compiler;
import com.fasterxml.jackson.databind.JsonNode;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ReportTheme;
import pl.lib.model.*;
import java.util.ArrayList;
import java.util.List;
public class SubreportCompiler {
    public JasperReport compileTableSubreport(JsonNode tableData, int availableWidth) throws JRException {
        if (!tableData.isArray() || tableData.isEmpty()) {
            throw new JRException("Table data must be a non-empty array");
        }
        JsonNode firstRow = tableData.get(0);
        if (!firstRow.isObject()) {
            throw new JRException("Table rows must be objects");
        }
        List<String> columnNames = new ArrayList<>();
        firstRow.fieldNames().forEachRemaining(columnNames::add);
        ReportBuilder tableBuilder = new ReportBuilder("TableSubreport").withTheme(ReportTheme.DEFAULT).withTitleBand(false).withPageFooter(false).withSummaryBand(false).withMargins(0, 0, 0, 0).withColumnWidth(availableWidth);
        Style tableHeaderStyle = new Style(ReportStyles.HEADER_STYLE).withFont(ReportStyles.FONT_DEJAVU_SANS, 7, true).withColors("#000000", "#E3E3E3").withAlignment("CENTER", "MIDDLE").withBorders(0.5f, "#CCCCCC").withPadding(1);
        tableBuilder.addStyle(tableHeaderStyle);
        for (String columnName : columnNames) {
            tableBuilder.addColumn(new Column(columnName, columnName, -1, DataType.STRING, null, Calculation.NONE, Calculation.NONE, ReportStyles.DATA_STYLE));
        }
        tableBuilder.calculateColumnWidths();
        return tableBuilder.build();
    }
}
