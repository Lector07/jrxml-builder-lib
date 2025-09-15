package pl.lib.config;

import pl.lib.model.Calculation;

import java.util.Objects;

/**
 * Column definition in a report.
 *
 * <p>This class describes a single column in the report, containing information about data field,
 * header, width, formatting and group/report calculations.</p>
 *
 * <p>Uses Builder pattern for convenient creation of instances with optional fields.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * ColumnDefinition column = ColumnDefinition.builder("price")
 *     .header("Product Price")
 *     .width(100)
 *     .format("#,##0.00 $")
 *     .reportCalculation(Calculation.SUM)
 *     .groupCalculation(Calculation.AVERAGE)
 *     .visible(true)
 *     .build();
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see Calculation
 */
public class ColumnDefinition {
    private String field;
    private String header;
    private Integer width = -1;
    private String format;
    private Calculation reportCalculation;
    private Calculation groupCalculation;
    private Boolean visible;
    private String type = "COLUMN"; // Added field to distinguish between regular columns and subreports

    /**
     * Column types enum defining the type of column
     */
    public static class ColumnType {
        public static final String COLUMN = "COLUMN";
        public static final String SUBREPORT = "SUBREPORT";
    }

    /**
     * Default constructor for JSON deserialization.
     */
    public ColumnDefinition(){

    }

    /**
     * Private constructor used by Builder.
     *
     * @param builder builder instance with set values
     */
    private ColumnDefinition(Builder builder) {
        this.field = Objects.requireNonNull(builder.field, "field");
        this.header = builder.header != null ? builder.header : this.field;
        this.width = builder.width;
        this.format = builder.format;
        this.reportCalculation = builder.reportCalculation;
        this.groupCalculation = builder.groupCalculation;
        this.visible = builder.visible;
        this.type = builder.type != null ? builder.type : this.type;
    }

    /**
     * Checks if column is visible in the report.
     *
     * @return true if column is visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Returns the data field name for this column.
     *
     * @return field name in source data
     */
    public String getField() {
        return field;
    }

    /**
     * Returns the column header displayed in the report.
     *
     * @return column header text
     */
    public String getHeader() {
        return header;
    }

    /**
     * Returns the column width in pixels.
     *
     * @return column width (-1 means automatic width)
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Returns the formatting pattern for values in the column.
     *
     * @return formatting pattern (e.g. "#,##0.00" for numbers)
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns the type of calculations performed at the report level.
     *
     * @return report calculation type
     */
    public Calculation getReportCalculation() {
        return reportCalculation;
    }

    /**
     * Returns the type of calculations performed at the group level.
     *
     * @return group calculation type
     */
    public Calculation getGroupCalculation() {
        return groupCalculation;
    }

    /**
     * Returns information about column visibility.
     *
     * @return Boolean determining column visibility
     */
    public Boolean getVisible() {
        return visible;
    }

    /**
     * Returns the type of the column (regular or subreport).
     *
     * @return column type
     */
    public String getType() {
        return type;
    }

    /**
     * Creates a new Builder for building ColumnDefinition instance.
     *
     * @param field data field name (required)
     * @return new Builder
     */
    public static Builder builder(String field) {
        return new Builder(field);
    }

    // Setters for JSON deserialization

    /**
     * Sets the data field name.
     *
     * @param field field name
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Sets the column header.
     *
     * @param header column header
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Sets the column width.
     *
     * @param width width in pixels
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * Sets the formatting pattern.
     *
     * @param format formatting pattern
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Sets the report calculation type.
     *
     * @param reportCalculation report calculation type
     */
    public void setReportCalculation(Calculation reportCalculation) {
        this.reportCalculation = reportCalculation;
    }

    /**
     * Sets the group calculation type.
     *
     * @param groupCalculation group calculation type
     */
    public void setGroupCalculation(Calculation groupCalculation) {
        this.groupCalculation = groupCalculation;
    }

    /**
     * Sets column visibility.
     *
     * @param visible whether column should be visible
     */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    /**
     * Sets the type of the column.
     *
     * @param type column type (e.g. "COLUMN" or "SUBREPORT")
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Builder for ColumnDefinition class implementing Builder pattern.
     *
     * <p>Enables step-by-step building of ColumnDefinition object with optional fields.</p>
     */
    public static class Builder {
        private final String field;
        private String header;
        private Integer width;
        private String format;
        private Calculation reportCalculation;
        private Calculation groupCalculation;
        private Boolean visible;
        private String type;

        /**
         * Builder constructor with required field name.
         *
         * @param field data field name (cannot be null)
         */
        public Builder(String field) {
            this.field = field;
        }

        /**
         * Sets the column header.
         *
         * @param header column header
         * @return this Builder (for method chaining)
         */
        public Builder header(String header) {
            this.header = header;
            return this;
        }

        /**
         * Sets the column width in pixels.
         *
         * @param width column width (-1 for automatic width)
         * @return this Builder (for method chaining)
         */
        public Builder width(Integer width) {
            this.width = width;
            return this;
        }

        /**
         * Sets the formatting pattern for values in the column.
         *
         * @param format formatting pattern (e.g. "#,##0.00" for numbers)
         * @return this Builder (for method chaining)
         */
        public Builder format(String format) {
            this.format = format;
            return this;
        }

        /**
         * Sets the type of calculations performed at the report level.
         *
         * @param reportCalculation report calculation type
         * @return this Builder (for method chaining)
         */
        public Builder reportCalculation(Calculation reportCalculation){
            this.reportCalculation = reportCalculation;
            return this;
        }

        /**
         * Sets the type of calculations performed at the group level.
         *
         * @param groupCalculation group calculation type
         * @return this Builder (for method chaining)
         */
        public Builder groupCalculation(Calculation groupCalculation){
            this.groupCalculation = groupCalculation;
            return this;
        }

        /**
         * Sets column visibility in the report.
         *
         * @param visible whether column should be visible
         * @return this Builder (for method chaining)
         */
        public Builder visible(Boolean visible){
            this.visible = visible;
            return this;
        }

        /**
         * Sets the type of the column.
         *
         * @param type column type (e.g. "COLUMN" or "SUBREPORT")
         * @return this Builder (for method chaining)
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Builds the final ColumnDefinition instance.
         *
         * @return new ColumnDefinition instance with set values
         */
        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }
    }
}
