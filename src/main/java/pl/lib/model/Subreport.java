package pl.lib.model;

import net.sf.jasperreports.engine.JasperReport;

/**
 * Represents a subreport in the main report structure.
 *
 * <p>This class holds a reference to a compiled JasperReports report
 * and the name of the data field that will be the data source for this subreport.</p>
 *
 * <p>Subreports are used to display hierarchical data or
 * detailed information related to each record of the main report.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * // Compiled subreport
 * JasperReport compiledSubreport = JasperCompileManager.compileReport(subreportDesign);
 *
 * // Create Subreport instance
 * Subreport subreport = new Subreport("details", compiledSubreport);
 *
 * // Add to main report
 * reportBuilder.addSubreport(subreport);
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see JasperReport
 * @see pl.lib.api.ReportBuilder#addSubreport(Subreport)
 */
public final class Subreport {
    private final String fieldName;
    private final JasperReport subreport;
    private final int height;
    private boolean showSummary;

    /**
     * Creates a new Subreport instance with default height (50 pixels).
     *
     * @param fieldName name of the field in source data containing data for the subreport
     * @param subreport compiled JasperReports report to be used as subreport
     * @throws IllegalArgumentException if fieldName is null or empty
     * @throws IllegalArgumentException if subreport is null
     */
    public Subreport(String fieldName, JasperReport subreport) {
        this(fieldName, subreport, 50, false);
    }

    /**
     * Creates a new Subreport instance with specified height.
     *
     * @param fieldName name of the field in source data containing data for the subreport
     * @param subreport compiled JasperReports report to be used as subreport
     * @param height height of the subreport band in pixels
     * @throws IllegalArgumentException if fieldName is null or empty
     * @throws IllegalArgumentException if subreport is null
     * @throws IllegalArgumentException if height is less than 1
     */
    public Subreport(String fieldName, JasperReport subreport, int height, boolean showSummary) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        if (subreport == null) {
            throw new IllegalArgumentException("Subreport cannot be null");
        }
        if (height < 1) {
            throw new IllegalArgumentException("Height must be at least 1 pixel");
        }
        this.fieldName = fieldName;
        this.subreport = subreport;
        this.height = height;
        this.showSummary = showSummary;
    }

    /**
     * Returns the data field name for this subreport.
     *
     * <p>The field name corresponds to a key in JSON data that contains
     * an array or object with data for the subreport.</p>
     *
     * @return data field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the compiled JasperReports report used as subreport.
     *
     * @return JasperReport instance
     */
    public JasperReport getSubreport() {
        return subreport;
    }

    /**
     * Returns the height of the subreport band in pixels.
     *
     * @return height in pixels
     */
    public int getHeight() {
        return height;
    }


    /**
     * Indicates whether to show the summary section of the subreport.
     *
     * @return true if the summary section should be shown, false otherwise
     */
    public boolean isShowSummary() {
        return showSummary;
    }
}