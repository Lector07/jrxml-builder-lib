package pl.lib.model;

import net.sf.jasperreports.engine.JRDataSource;
import java.awt.Image;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Enum defining supported data types in reports.
 *
 * <p>Specifies mapping between JSON data types and Java classes used
 * in JasperReports. Each type has an assigned corresponding Java class.</p>
 *
 * <h3>Supported data types:</h3>
 * <ul>
 *   <li><b>STRING</b> - text (String)</li>
 *   <li><b>INTEGER</b> - integers (Integer)</li>
 *   <li><b>LONG</b> - large integers (Long)</li>
 *   <li><b>DOUBLE</b> - floating point numbers (Double)</li>
 *   <li><b>BIG_DECIMAL</b> - precise decimal numbers (BigDecimal)</li>
 *   <li><b>FLOAT</b> - single precision floating point (Float)</li>
 *   <li><b>SHORT</b> - short integers (Short)</li>
 *   <li><b>DATE</b> - dates (Date)</li>
 *   <li><b>TIME</b> - time (Time)</li>
 *   <li><b>TIMESTAMP</b> - timestamp (Timestamp)</li>
 *   <li><b>BOOLEAN</b> - boolean values (Boolean)</li>
 *   <li><b>IMAGE</b> - images (Image)</li>
 *   <li><b>JR_DATA_SOURCE</b> - data sources for subreports (JRDataSource)</li>
 * </ul>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * DataType type = DataType.BIG_DECIMAL;
 * Class<?> javaClass = type.getJavaClass(); // BigDecimal.class
 * boolean isNumber = type.isNumeric(); // true
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 * @see pl.lib.model.Column
 */
public enum DataType {
    /** Text type */
    STRING(String.class),

    /** Integers */
    INTEGER(Integer.class),

    /** Large integers */
    LONG(Long.class),

    /** Double precision floating point numbers */
    DOUBLE(Double.class),

    /** Dates */
    DATE(Date.class),

    /** Boolean values */
    BOOLEAN(Boolean.class),

    /** Precise decimal numbers */
    BIG_DECIMAL(BigDecimal.class),

    /** Short integers */
    SHORT(Short.class),

    /** Time */
    TIME(Time.class),

    /** Timestamp */
    TIMESTAMP(Timestamp.class),

    /** Single precision floating point numbers */
    FLOAT(Float.class),

    /** Images */
    IMAGE(Image.class),

    /** Data sources for subreports */
    JR_DATA_SOURCE(JRDataSource.class);

    private final Class<?> javaClass;

    /**
     * Enum constructor with assigned Java class.
     *
     * @param javaClass Java class corresponding to this data type
     */
    DataType(Class<?> javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * Returns the Java class corresponding to this data type.
     *
     * @return Java class
     */
    public Class<?> getJavaClass() {
        return javaClass;
    }

    /**
     * Checks if the data type is numeric.
     *
     * <p>A type is considered numeric if mathematical operations
     * and numeric comparisons can be performed on it.</p>
     *
     * @return true for numeric types, false otherwise
     */
    public boolean isNumeric() {
        return this == INTEGER || this == LONG || this == DOUBLE || this == BIG_DECIMAL || this == SHORT || this == FLOAT;
    }
}