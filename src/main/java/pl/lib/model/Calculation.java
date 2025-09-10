package pl.lib.model;

/**
 * Enum defining available calculation types for report columns.
 *
 * <p>Specifies which aggregate functions can be applied to column values
 * at group or report level. Each calculation type has a corresponding
 * function in JasperReports.</p>
 *
 * <h3>Available calculation types:</h3>
 * <ul>
 *   <li><b>NONE</b> - no calculations</li>
 *   <li><b>COUNT</b> - count of records</li>
 *   <li><b>SUM</b> - sum of values</li>
 *   <li><b>AVERAGE</b> - arithmetic mean</li>
 *   <li><b>LOWEST</b> - lowest value</li>
 *   <li><b>HIGHEST</b> - highest value</li>
 *   <li><b>STANDARD_DEVIATION</b> - standard deviation</li>
 *   <li><b>VARIANCE</b> - variance</li>
 *   <li><b>DISTINCT_COUNT</b> - count of unique values</li>
 *   <li><b>FIRST</b> - first value</li>
 * </ul>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * ColumnDefinition column = ColumnDefinition.builder("price")
 *     .header("Price")
 *     .reportCalculation(Calculation.SUM)
 *     .groupCalculation(Calculation.AVERAGE)
 *     .build();
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.config.ColumnDefinition
 */
public enum Calculation {
    /** No aggregate calculations */
    NONE("None"),

    /** Count of records */
    COUNT("Count"),

    /** Sum of numeric values */
    SUM("Sum"),

    /** Arithmetic mean */
    AVERAGE("Average"),

    /** Lowest value */
    LOWEST("Lowest"),

    /** Highest value */
    HIGHEST("Highest"),

    /** Standard deviation */
    STANDARD_DEVIATION("StandardDeviation"),

    /** Variance */
    VARIANCE("Variance"),

    /** Count of unique values */
    DISTINCT_COUNT("DistinctCount"),

    /** First value in group */
    FIRST("First");

    private final String jasperFunctionName;

    /**
     * Enum constructor with JasperReports function name.
     *
     * @param jasperFunctionName JasperReports function name
     */
    Calculation(String jasperFunctionName) {
        this.jasperFunctionName = jasperFunctionName;
    }

    /**
     * Returns the corresponding JasperReports function name.
     *
     * @return JasperReports function name
     */
    public String getJasperFunctionName() {
        return jasperFunctionName;
    }

    /**
     * Checks if this calculation is active (different from NONE).
     *
     * @return true if calculation is active, false for NONE
     */
    public boolean isActive(){
        return this != NONE;
    }
}
