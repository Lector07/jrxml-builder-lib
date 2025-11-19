package pl.lib.model;
public enum Calculation {
    NONE("None"),
    COUNT("Count"),
    SUM("Sum"),
    AVERAGE("Average"),
    LOWEST("Lowest"),
    HIGHEST("Highest"),
    STANDARD_DEVIATION("StandardDeviation"),
    VARIANCE("Variance"),
    DISTINCT_COUNT("DistinctCount"),
    FIRST("First");
    private final String jasperFunctionName;
    Calculation(String jasperFunctionName) {
        this.jasperFunctionName = jasperFunctionName;
    }
    public String getJasperFunctionName() {
        return jasperFunctionName;
    }
    public boolean isActive(){
        return this != NONE;
    }
}
