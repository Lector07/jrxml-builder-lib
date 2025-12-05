package pl.lib.automation.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import pl.lib.api.ReportBuilder;
import pl.lib.config.ReportTheme;
import pl.lib.model.*;
import pl.lib.automation.util.HeaderFormatter;
import pl.lib.automation.util.JsonDataTypeGuesser;

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

        ReportBuilder tableBuilder = new ReportBuilder("TableSubreport")
                .withTheme(ReportTheme.DEFAULT)
                .withTitleBand(false)
                .withPageFooter(false)
                .withSummaryBand(false)
                .withMargins(0, 0, 0, 0)
                .withColumnWidth(availableWidth);

        Style tableHeaderStyle = new Style(ReportStyles.HEADER_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS_CONDENSED, 9, true)
                .withColors("#1C3A57", "#E8EEF4")
                .withAlignment("CENTER", "MIDDLE")
                .withBorders(0.5f, "#C0D6E8")
                .withPadding(5);
        tableBuilder.addStyle(tableHeaderStyle);


        Style tableDataStyle = new Style(ReportStyles.DATA_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS_CONDENSED, 8, false)
                .withColors("#2C3E50", "#FFFFFF")
                .withAlignment("LEFT", "MIDDLE")
                .withBorders(0.5f, "#E8EEF4")
                .withPadding(4);
        tableBuilder.addStyle(tableDataStyle);

        Style tableNumericStyle = new Style(ReportStyles.NUMERIC_STYLE)
                .withFont(ReportStyles.FONT_DEJAVU_SANS_CONDENSED, 8, false)
                .withColors("#2C3E50", "#FFFFFF")
                .withAlignment("RIGHT", "MIDDLE")
                .withBorders(0.5f, "#E8EEF4")
                .withPadding(4);
        tableBuilder.addStyle(tableNumericStyle);

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
