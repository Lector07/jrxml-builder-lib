package pl.lib.automation.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ReportTheme;
import pl.lib.model.*;
import pl.lib.automation.util.HeaderFormatter;
import pl.lib.automation.util.JsonDataTypeGuesser;

import java.util.ArrayList;
import java.util.Iterator;
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

        ReportBuilder tableBuilder = new ReportBuilder("TableSubreport")
                .withTheme(ReportTheme.DEFAULT)
                .withTitleBand(false)
                .withPageFooter(false)
                .withSummaryBand(false)
                .withMargins(0, 0, 0, 0)
                .withColumnWidth(availableWidth);

               Style tableHeaderStyle = new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS, 9, true)
                .withColors("#FFFFFF", "#34495E")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(1f, "#2C3E50")
                .withPadding(3);
        tableBuilder.addStyle(tableHeaderStyle);

        for (String columnName : columnNames) {
            String formattedHeader = HeaderFormatter.formatHeaderName(columnName);
            DataType dataType = JsonDataTypeGuesser.guessType(tableData, columnName);

            tableBuilder.addColumn(new Column(
                    columnName,
                    formattedHeader,
                    -1,
                    dataType,
                    null,
                    Calculation.NONE,
                    Calculation.NONE,
                    ReportStyles.DATA_STYLE
            ));
        }

        tableBuilder.calculateColumnWidths();
        return tableBuilder.build();
    }
}
