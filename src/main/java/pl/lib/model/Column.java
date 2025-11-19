package pl.lib.model;
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
    public Column(){
    }
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
    public Column(String fieldName, String title, int width, DataType type, String pattern,
                  Calculation reportCalculation, Calculation groupCalculation, String styleName) {
        this(fieldName, title, width, type, pattern, reportCalculation, groupCalculation, styleName, false);
    }
    public Column withBox(boolean hasBox) {
        return new Column(this.fieldName, this.title, this.width, this.type, this.pattern,
                this.reportCalculation, this.groupCalculation, this.styleName, hasBox);
    }
    public Column withWidth(int width) {
        return new Column(this.fieldName, this.title, width, this.type, this.pattern,
                this.reportCalculation, this.groupCalculation, this.styleName, this.hasBox);
    }
    public String getFieldName() { return fieldName; }
    public String getTitle() { return title; }
    public int getWidth() { return width; }
    public DataType getType() { return type; }
    public String getPattern() { return pattern; }
    public boolean hasPattern() { return pattern != null && !pattern.isEmpty(); }
    public Calculation getReportCalculation() { return reportCalculation; }
    public Calculation getGroupCalculation() { return groupCalculation; }
    public String getStyleName() { return styleName; }
    public boolean hasStyle() { return styleName != null && !styleName.isEmpty(); }
    public boolean hasReportCalculation() { return reportCalculation != null && reportCalculation.isActive(); }
    public boolean hasGroupCalculation() { return groupCalculation != null && groupCalculation.isActive(); }
    public boolean hasBox() { return hasBox; }
    public DataType getDataType() { return type; }
}