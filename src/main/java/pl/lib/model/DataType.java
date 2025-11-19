package pl.lib.model;
import net.sf.jasperreports.engine.JRDataSource;
import java.awt.Image;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
public enum DataType {
    STRING(String.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    DATE(Date.class),
    BOOLEAN(Boolean.class),
    BIG_DECIMAL(BigDecimal.class),
    SHORT(Short.class),
    TIME(Time.class),
    TIMESTAMP(Timestamp.class),
    FLOAT(Float.class),
    IMAGE(Image.class),
    JR_DATA_SOURCE(JRDataSource.class);
    private final Class<?> javaClass;
    DataType(Class<?> javaClass) {
        this.javaClass = javaClass;
    }
    public Class<?> getJavaClass() {
        return javaClass;
    }
    public boolean isNumeric() {
        return this == INTEGER || this == LONG || this == DOUBLE || this == BIG_DECIMAL || this == SHORT || this == FLOAT;
    }
}