package pl.lib.model;

public enum DataType {
    STRING("java.lang.String"),
    INTEGER("java.lang.Integer"),
    LONG("java.lang.Long"),
    DOUBLE("java.lang.Double"),
    DATE("java.util.Date"),
    BOOLEAN("java.lang.Boolean"),
    BIG_DECIMAL("java.math.BigDecimal"),
    SHORT("java.lang.Short"),
    TIME("java.sql.Time"),
    TIMESTAMP("java.sql.Timestamp"),
    FLOAT("java.lang.Float");
    private final String javaClass;

    DataType(String javaClass) {
        this.javaClass = javaClass;
    }

    public String getJavaClass() {
        return javaClass;
    }

}
