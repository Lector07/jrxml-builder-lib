/**
 * Model package containing data classes and types used in reports.
 *
 * <p>This package contains all data model classes, type enumerations and
 * classes representing report elements. It forms the foundation of data types
 * used throughout the library.</p>
 *
 * <h2>Data model classes:</h2>
 * <ul>
 *   <li>{@link pl.lib.model.CompanyInfo} - Company information for report header</li>
 *   <li>{@link pl.lib.model.Column} - Report column definition</li>
 *   <li>{@link pl.lib.model.Group} - Group definition in report</li>
 *   <li>{@link pl.lib.model.Style} - Formatting style definition</li>
 *   <li>{@link pl.lib.model.Image} - Image representation in report</li>
 *   <li>{@link pl.lib.model.Subreport} - Subreport definition</li>
 * </ul>
 *
 * <h2>Type enumerations:</h2>
 * <ul>
 *   <li>{@link pl.lib.model.DataType} - Supported data types (String, BigDecimal, Date, etc.)</li>
 *   <li>{@link pl.lib.model.Calculation} - Aggregate calculation types (SUM, COUNT, AVERAGE, etc.)</li>
 * </ul>
 *
 * <h2>Helper classes:</h2>
 * <ul>
 *   <li>{@link pl.lib.model.ReportStyles} - Style and font name constants</li>
 * </ul>
 *
 * <h2>Data types and conversions:</h2>
 * <p>The library automatically detects and converts data types from JSON:</p>
 * <ul>
 *   <li><strong>STRING</strong> - text, automatically detects ISO 8601 dates</li>
 *   <li><strong>BIG_DECIMAL</strong> - numbers with precision preservation</li>
 *   <li><strong>DATE</strong> - dates in ISO 8601 format</li>
 *   <li><strong>BOOLEAN</strong> - boolean values</li>
 *   <li><strong>JR_DATA_SOURCE</strong> - nested arrays for subreports</li>
 * </ul>
 *
 * <h2>Aggregate calculations:</h2>
 * <p>The library supports various calculation types at group and report level:</p>
 * <ul>
 *   <li><strong>SUM</strong> - sum of values (for numeric data)</li>
 *   <li><strong>COUNT</strong> - count of records</li>
 *   <li><strong>AVERAGE</strong> - arithmetic mean</li>
 *   <li><strong>LOWEST/HIGHEST</strong> - minimum/maximum values</li>
 *   <li><strong>DISTINCT_COUNT</strong> - count of unique values</li>
 * </ul>
 *
 * <h2>Usage example of types:</h2>
 * <pre>{@code
 * // Column definition with calculations
 * Column priceColumn = new Column(
 *     "price",                    // field name
 *     "Product Price",           // header
 *     100,                       // width
 *     DataType.BIG_DECIMAL,      // data type
 *     "#,##0.00 $",             // format
 *     Calculation.SUM,           // report level calculation
 *     Calculation.AVERAGE,       // group level calculation
 *     "numericStyle"             // style name
 * );
 *
 * // Group definition
 * Group categoryGroup = new Group(
 *     "category",                // grouping field
 *     "Category: ",             // label
 *     "groupStyle",             // style
 *     true,                     // show group footer
 *     true                      // show group header
 * );
 *
 * // Company information
 * CompanyInfo company = CompanyInfo.builder("ACME Corp")
 *     .address("123 Business St")
 *     .location("00-001", "New York")
 *     .contact("123-456-789", "office@acme.com")
 *     .build();
 * }</pre>
 *
 * <h2>JasperReports integration:</h2>
 * <p>Model classes are designed for direct integration with JasperReports:</p>
 * <ul>
 *   <li>Data types map to Java classes used by JasperReports</li>
 *   <li>Aggregate calculations correspond to JasperReports functions</li>
 *   <li>Styles are converted to JRStyle objects</li>
 *   <li>Subreports use JRDataSource for nested data</li>
 * </ul>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.config
 * @see pl.lib.api.ReportBuilder
 * @see net.sf.jasperreports.engine
 */
package pl.lib.model;
