/**
 * Main package of the JRXML Builder Library.
 *
 * <p>The JRXML Builder Library is a complete solution for automatic
 * generation of JasperReports from JSON data. It offers high-level API
 * and flexible configuration options for creating professional PDF reports.</p>
 *
 * <h2>Library architecture:</h2>
 * <ul>
 *   <li>{@link pl.lib.api} - Main API for building reports</li>
 *   <li>{@link pl.lib.automation} - Automatic generation from JSON</li>
 *   <li>{@link pl.lib.config} - Configuration classes</li>
 *   <li>{@link pl.lib.model} - Data models and types</li>
 * </ul>
 *
 * <h2>Main features:</h2>
 * <ul>
 *   <li>Automatic JSON parsing and structure analysis</li>
 *   <li>Report generation with grouping and calculations</li>
 *   <li>Subreport support for hierarchical data</li>
 *   <li>Flexible formatting and styling</li>
 *   <li>Builder pattern for easy configuration</li>
 *   <li>Support for different page orientations and margins</li>
 *   <li>Cell highlighting based on rules</li>
 *   <li>PDF bookmark generation</li>
 * </ul>
 *
 * <h2>Usage example:</h2>
 * <pre>{@code
 * // JSON data
 * String jsonData = "[{\"name\":\"Product A\",\"price\":100.50,\"category\":\"Electronics\"}]";
 *
 * // Report configuration
 * ReportConfig config = new ReportConfig.Builder()
 *     .title("Products Report")
 *     .addColumn(ColumnDefinition.builder("name").header("Product Name").build())
 *     .addColumn(ColumnDefinition.builder("price").header("Price").format("#,##0.00 $").build())
 *     .addGroup(GroupDefinition.builder("category").label("Category: ").build())
 *     .companyInfo(CompanyInfo.builder("My Company").build())
 *     .build();
 *
 * // Generate report
 * AutomatedReportService service = new AutomatedReportService();
 * service.generatePdfReportFromJson(jsonData, config, "report.pdf");
 * }</pre>
 *
 * @author BIURO USLUG KOMPUTEROWYCH "SOFTRES" SP Z O.O
 * @version 1.0-SNAPSHOT
 * @since 1.0
 */
package pl.lib;
