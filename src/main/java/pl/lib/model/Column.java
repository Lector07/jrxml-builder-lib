package pl.lib.model;

/**
 * Represents a column in a report with its properties and calculations.
 *
 * <p>This class defines a single column in the report including field mapping,
 * display properties, formatting options, and calculation settings for both
 * group and report levels.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * Column priceColumn = new Column(
 *     "price",                    // field name in data source
 *     "Product Price",           // column header
 *     120,                       // column width in pixels
 *     DataType.BIG_DECIMAL,      // data type
 *     "#,##0.00 $",             // formatting pattern
 *     Calculation.SUM,           // report-level calculation
 *     Calculation.AVERAGE,       // group-level calculation
 *     "numericStyle"             // style name
 * );
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see DataType
 * @see Calculation
 */
public final class Column {
    private String fieldName;
    private String title;
    private int width;
    private DataType type;
    private String pattern;
    private Calculation reportCalculation;
    private Calculation groupCalculation;
    private String styleName;
    private boolean hasBox;

    /**
     * Default constructor for JSON deserialization.
     */
    public Column(){

    }

    /**
     * Creates a new Column with all properties including box setting.
     *
     * @param fieldName name of the field in data source
     * @param title column header text
     * @param width column width in pixels
     * @param type data type for the column
     * @param pattern formatting pattern (e.g., "#,##0.00" for numbers)
     * @param reportCalculation calculation to perform at report level
     * @param groupCalculation calculation to perform at group level
     * @param styleName name of the style to apply
     * @param hasBox whether column should have border box
     */
    public Column(String fieldName, String title, int width, DataType type, String pattern,
                  Calculation reportCalculation, Calculation groupCalculation, String styleName, boolean hasBox) {
        this.fieldName = fieldName;
        this.title = title;
        this.width = width;
        this.type = type;
        this.pattern = pattern;
        this.reportCalculation = reportCalculation;
        this.groupCalculation = groupCalculation;
        this.styleName = styleName;
        this.hasBox = hasBox;
    }

    /**
     * Creates a new Column with default box setting (no box).
     *
     * @param fieldName name of the field in data source
     * @param title column header text
     * @param width column width in pixels
     * @param type data type for the column
     * @param pattern formatting pattern (e.g., "#,##0.00" for numbers)
     * @param reportCalculation calculation to perform at report level
     * @param groupCalculation calculation to perform at group level
     * @param styleName name of the style to apply
     */
    public Column(String fieldName, String title, int width, DataType type, String pattern,
                  Calculation reportCalculation, Calculation groupCalculation, String styleName) {
        this(fieldName, title, width, type, pattern, reportCalculation, groupCalculation, styleName, false);
    }

    /**
     * Creates a copy of this column with modified box setting.
     *
     * @param hasBox whether the new column should have border box
     * @return new Column instance with updated box setting
     */
    public Column withBox(boolean hasBox) {
        return new Column(this.fieldName, this.title, this.width, this.type, this.pattern,
                this.reportCalculation, this.groupCalculation, this.styleName, hasBox);
    }

    /**
     * Creates a copy of this column with modified width.
     *
     * @param width new column width in pixels
     * @return new Column instance with updated width
     */
    public Column withWidth(int width) {
        return new Column(this.fieldName, this.title, width, this.type, this.pattern,
                this.reportCalculation, this.groupCalculation, this.styleName, this.hasBox);
    }

    /**
     * Returns the field name in the data source.
     *
     * @return field name
     */
    public String getFieldName() { return fieldName; }

    /**
     * Returns the column header text.
     *
     * @return column title
     */
    public String getTitle() { return title; }

    /**
     * Returns the column width in pixels.
     *
     * @return column width
     */
    public int getWidth() { return width; }

    /**
     * Returns the data type for this column.
     *
     * @return data type
     */
    public DataType getType() { return type; }

    /**
     * Returns the formatting pattern for values in this column.
     *
     * @return formatting pattern
     */
    public String getPattern() { return pattern; }

    /**
     * Checks if this column has a formatting pattern.
     *
     * @return true if pattern is defined and not empty
     */
    public boolean hasPattern() { return pattern != null && !pattern.isEmpty(); }

    /**
     * Returns the calculation to perform at report level.
     *
     * @return report calculation
     */
    public Calculation getReportCalculation() { return reportCalculation; }

    /**
     * Returns the calculation to perform at group level.
     *
     * @return group calculation
     */
    public Calculation getGroupCalculation() { return groupCalculation; }

    /**
     * Returns the style name for this column.
     *
     * @return style name
     */
    public String getStyleName() { return styleName; }

    /**
     * Checks if this column has a style defined.
     *
     * @return true if style name is defined and not empty
     */
    public boolean hasStyle() { return styleName != null && !styleName.isEmpty(); }

    /**
     * Checks if this column has report-level calculations.
     *
     * @return true if report calculation is active
     */
    public boolean hasReportCalculation() { return reportCalculation != null && reportCalculation.isActive(); }

    /**
     * Checks if this column has group-level calculations.
     *
     * @return true if group calculation is active
     */
    public boolean hasGroupCalculation() { return groupCalculation != null && groupCalculation.isActive(); }

    /**
     * Checks if this column should have border box.
     *
     * @return true if column has box
     */
    public boolean hasBox() { return hasBox; }

    /**
     * Returns the data type for this column.
     *
     * @return data type
     */
    public DataType getDataType() { return type; }
}