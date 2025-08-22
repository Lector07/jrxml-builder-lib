package pl.lib.model;

public enum Calculation {
    SUM("Sum"),
    AVERAGE("Average"),
    COUNT("Count"),
    LOWEST("Min"),
    HIGHEST("Max");

    private final String jasperFunctionName;
    Calculation(String jasperFunctionName) {this.jasperFunctionName = jasperFunctionName;}
    public String getJasperFunctionName() {return jasperFunctionName;}

}
