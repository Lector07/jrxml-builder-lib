/**
 * API package of the library containing main classes for building reports.
 *
 * <p>This package contains key classes responsible for programmatic creation
 * of JasperReports reports. The main class is {@link pl.lib.api.ReportBuilder},
 * which implements Builder pattern for step-by-step report construction.</p>
 *
 * <h2>Main classes:</h2>
 * <ul>
 *   <li>{@link pl.lib.api.ReportBuilder} - Builder for creating JasperReports reports</li>
 * </ul>
 *
 * <h2>Functionalities:</h2>
 * <ul>
 *   <li>Programmatic report structure definition</li>
 *   <li>Adding columns with formatting and calculations</li>
 *   <li>Data grouping configuration</li>
 *   <li>Adding styles and formatting</li>
 *   <li>Subreports support</li>
 *   <li>Margins and page orientation configuration</li>
 * </ul>
 *
 * <h2>ReportBuilder usage example:</h2>
 * <pre>{@code
 * ReportBuilder builder = new ReportBuilder()
 *     .withTitle("Sales Report")
 *     .withHorizontalLayout(true)
 *     .addColumn(new Column("name", "Product Name", 200, DataType.STRING))
 *     .addColumn(new Column("price", "Price", 100, DataType.BIG_DECIMAL, "#,##0.00"))
 *     .addGroup(new Group("category", "Category: ", "groupStyle"))
 *     .addStyle(new Style("groupStyle").withFont("Arial", 10, true));
 *
 * JasperReport report = builder.build();
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.automation
 * @see pl.lib.config
 * @see pl.lib.model
 */
package pl.lib.api;
