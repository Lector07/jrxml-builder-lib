/**
 * Configuration package containing classes for defining report settings.
 *
 * <p>This package contains all configuration classes used to define
 * structure, appearance and behavior of reports. Classes utilize Builder pattern
 * and support JSON deserialization for flexible configuration.</p>
 *
 * <h2>Main configuration classes:</h2>
 * <ul>
 *   <li>{@link pl.lib.config.ReportConfig} - Main report configuration</li>
 *   <li>{@link pl.lib.config.ColumnDefinition} - Column definition with formatting</li>
 *   <li>{@link pl.lib.config.GroupDefinition} - Data grouping definition</li>
 *   <li>{@link pl.lib.config.FormattingOptions} - Visual formatting options</li>
 *   <li>{@link pl.lib.config.HighlightRule} - Cell highlighting rules</li>
 * </ul>
 *
 * <h2>Configuration pattern:</h2>
 * <p>All main configuration classes implement Builder pattern,
 * which enables intuitive and readable settings definition:</p>
 *
 * <h3>Configuration functionalities:</h3>
 * <ul>
 *   <li>Defining columns with data types and formatting</li>
 *   <li>Grouping configuration with aggregate calculations</li>
 *   <li>Page orientation and margins settings</li>
 *   <li>Highlighting rules based on values</li>
 *   <li>PDF bookmarks generation options</li>
 *   <li>Zebra stripes for better readability</li>
 *   <li>Hierarchical subreports configuration</li>
 * </ul>
 *
 * <h2>Complex configuration example:</h2>
 * <pre>{@code
 * // Formatting options
 * FormattingOptions formatting = new FormattingOptions();
 * formatting.setZebraStripes(true);
 * formatting.setGenerateBookmarks(true);
 * formatting.setHighlightRules(Arrays.asList(
 *     new HighlightRule("price", "GREATER_THAN", "1000", "#FFCCCC")
 * ));
 *
 * // Main configuration
 * ReportConfig config = new ReportConfig.Builder()
 *     .title("Comprehensive sales report")
 *     .addColumn(ColumnDefinition.builder("product")
 *         .header("Product name")
 *         .width(200)
 *         .visible(true)
 *         .build())
 *     .addColumn(ColumnDefinition.builder("price")
 *         .header("Unit price")
 *         .format("#,##0.00 $")
 *         .reportCalculation(Calculation.SUM)
 *         .groupCalculation(Calculation.AVERAGE)
 *         .build())
 *     .addGroup(GroupDefinition.builder("category")
 *         .label("Product category: ")
 *         .showHeader(true)
 *         .showFooter(true)
 *         .ascending(true)
 *         .build())
 *     .companyInfo(CompanyInfo.builder("My Company")
 *         .address("123 Main Street")
 *         .location("00-001", "New York")
 *         .build())
 *     .addFormattingOption(formatting)
 *     .margins(Arrays.asList(20, 20, 20, 20))
 *     .withPageFooterEnabled(true)
 *     .build();
 * }</pre>
 *
 * <h2>JSON deserialization:</h2>
 * <p>All configuration classes support JSON deserialization
 * using Jackson, which allows storing configuration in files:</p>
 *
 * <pre>{@code
 * {
 *   "title": "Report from JSON",
 *   "columns": [
 *     {
 *       "field": "name",
 *       "header": "Product name",
 *       "width": 200,
 *       "visible": true
 *     }
 *   ],
 *   "formattingOptions": {
 *     "zebraStripes": true,
 *     "generateBookmarks": true
 *   }
 * }
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.model
 * @see pl.lib.automation.AutomatedReportService
 */
package pl.lib.config;
