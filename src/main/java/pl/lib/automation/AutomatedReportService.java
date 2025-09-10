package pl.lib.automation;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import pl.lib.config.ReportConfig;

import java.io.IOException;

/**
 * Service for automated report generation from JSON data.
 *
 * <p>This class serves as the main entry point to the library, offering high-level methods
 * for generating PDF reports directly from JSON data and report configuration.</p>
 *
 * <p>The service internally uses {@link JsonReportGenerator} for data processing
 * and JasperReports structure generation.</p>
 *
 * <h3>Main functionalities:</h3>
 * <ul>
 *   <li>PDF report generation from JSON</li>
 *   <li>Subreports and groups support</li>
 *   <li>Optional JRXML output for debugging</li>
 *   <li>Returning JasperPrint objects for further processing</li>
 * </ul>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * // JSON data as String
 * String jsonData = "[{\"name\":\"Product 1\",\"price\":100.50}]";
 *
 * // Report configuration
 * ReportConfig config = new ReportConfig.Builder()
 *     .title("Products Report")
 *     .addColumn(ColumnDefinition.builder("name").header("Product Name").build())
 *     .addColumn(ColumnDefinition.builder("price").header("Price").format("#,##0.00").build())
 *     .build();
 *
 * // Generate report
 * AutomatedReportService service = new AutomatedReportService();
 * service.generatePdfReportFromJson(jsonData, config, "report.pdf");
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see JsonReportGenerator
 * @see ReportConfig
 */
public class AutomatedReportService {
    private final JsonReportGenerator generator = new JsonReportGenerator();

    /**
     * Generates PDF report from JSON data and saves to file.
     *
     * <p>The method processes JSON data according to the provided configuration and exports
     * the resulting report directly to a PDF file.</p>
     *
     * @param jsonContent source data in JSON format (must be an array of objects)
     * @param config report configuration defining columns, groups and formatting
     * @param outputPath path to output PDF file
     * @throws JRException in case of compilation or report generation errors
     * @throws IOException in case of file write errors
     * @throws IllegalArgumentException if jsonContent is not a valid JSON array
     */
    public void generatePdfReportFromJson(String jsonContent, ReportConfig config, String outputPath)
            throws JRException, IOException {
        JasperPrint jasperPrint = generator.generateReportFromJson(jsonContent, config);
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
    }

    /**
     * Generates PDF report from JSON data with JRXML debugging option.
     *
     * <p>Similar to {@link #generatePdfReportFromJson(String, ReportConfig, String)},
     * but additionally prints the generated JRXML code to console for debugging purposes.</p>
     *
     * @param jsonContent source data in JSON format (must be an array of objects)
     * @param config report configuration defining columns, groups and formatting
     * @param outputPath path to output PDF file
     * @throws JRException in case of compilation or report generation errors
     * @throws IOException in case of file write errors
     * @throws IllegalArgumentException if jsonContent is not a valid JSON array
     */
    public void generatePdfReportFromJsonWithJrxml(String jsonContent, ReportConfig config, String outputPath)
            throws JRException, IOException {
        JsonReportGenerator generatorWithPrinting = new JsonReportGenerator().withJrxmlPrinting(true);
        JasperPrint jasperPrint = generatorWithPrinting.generateReportFromJson(jsonContent, config);
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
    }

    /**
     * Generates JasperPrint object from JSON data for further processing.
     *
     * <p>The method returns a JasperPrint object instead of saving the report to file,
     * which allows for further processing or export in different formats.</p>
     *
     * @param jsonContent source data in JSON format (must be an array of objects)
     * @param config report configuration defining columns, groups and formatting
     * @return JasperPrint object ready for export in various formats
     * @throws JRException in case of compilation or report generation errors
     * @throws IOException in case of JSON parsing errors
     * @throws IllegalArgumentException if jsonContent is not a valid JSON array
     */
    public JasperPrint generateReportFromJson(String jsonContent, ReportConfig config)
            throws JRException, IOException {
        return generator.generateReportFromJson(jsonContent, config);
    }

}