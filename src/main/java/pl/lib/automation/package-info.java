/**
 * Automation package containing classes for automatic report generation from JSON.
 *
 * <p>This package contains classes responsible for automatic JSON data analysis
 * and report generation without the need for manual structure definition.
 * The package offers high-level API for quick report creation.</p>
 *
 * <h2>Main classes:</h2>
 * <ul>
 *   <li>{@link pl.lib.automation.AutomatedReportService} - High-level service for PDF generation</li>
 *   <li>{@link pl.lib.automation.JsonReportGenerator} - Generator analyzing JSON and creating reports</li>
 * </ul>
 *
 * <h2>Automation functionalities:</h2>
 * <ul>
 *   <li>Automatic JSON structure analysis</li>
 *   <li>Data type detection (text, numbers, dates)</li>
 *   <li>Recursive processing of nested structures</li>
 *   <li>Automatic subreport generation</li>
 *   <li>ISO 8601 date conversion</li>
 *   <li>Data sorting and grouping</li>
 *   <li>Optional debugging with JRXML output</li>
 * </ul>
 *
 * <h2>Processing flow:</h2>
 * <ol>
 *   <li>JSON parsing and structure analysis</li>
 *   <li>Data type detection for each field</li>
 *   <li>Nested arrays identification (subreports)</li>
 *   <li>Main report structure generation</li>
 *   <li>Subreports compilation (if exist)</li>
 *   <li>Data sorting by groups</li>
 *   <li>Report filling with data and export</li>
 * </ol>
 *
 * <h2>Usage example:</h2>
 * <pre>{@code
 * // JSON data with nested structures
 * String jsonData = """
 *     [
 *       {
 *         "company": "ABC Corp",
 *         "category": "IT",
 *         "products": [
 *           {"name": "Laptop", "price": 2500.00},
 *           {"name": "Mouse", "price": 45.00}
 *         ]
 *       }
 *     ]
 *     """;
 *
 * // Configuration with subreports
 * ReportConfig config = new ReportConfig.Builder()
 *     .title("Companies with products report")
 *     .addColumn(ColumnDefinition.builder("company").header("Company Name").build())
 *     .addColumn(ColumnDefinition.builder("category").header("Industry").build())
 *     .withSubreportConfig("products", subReportConfig)
 *     .build();
 *
 * // Automatic generation
 * AutomatedReportService service = new AutomatedReportService();
 * service.generatePdfReportFromJson(jsonData, config, "companies_report.pdf");
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.config.ReportConfig
 * @see pl.lib.api.ReportBuilder
 */
package pl.lib.automation;
