package pl.lib.model;

public class Column {
    private final String fieldName;
    private final String title;
    private int width;
    private final DataType type;
    private  String pattern;
    private final Calculation reportCalculation;
    private final Calculation groupCalculation;
    private String styleName;
    private boolean hasBox = false;


    public Column(String fieldName, String title, int width, DataType type, String pattern,
                  Calculation reportCalculation, Calculation groupCalculation) {
        this.fieldName = fieldName;
        this.title = title;
        this.width = width;
        this.type = type;
        this.pattern = pattern;
        this.reportCalculation = reportCalculation;
        this.groupCalculation = groupCalculation;
        this.styleName = null;
    }

    public Column(String fieldName, String title, int width, DataType type, String pattern,
                  Calculation reportCalculation, Calculation groupCalculation, String styleName) {
        this(fieldName, title, width, type, pattern, reportCalculation, groupCalculation);
        this.styleName = styleName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public DataType getType() {
        return type;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean hasPattern() {
        return pattern != null && !pattern.isEmpty();
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Calculation getReportCalculation() {
        return reportCalculation;
    }

    public Calculation getGroupCalculation() {
        return groupCalculation;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public boolean hasStyle() {
        return styleName != null && !styleName.isEmpty();
    }

    public boolean hasReportCalculation() {
        return reportCalculation != null;
    }

    public boolean hasGroupCalculation() {
        return groupCalculation != null;
    }


    public Column withBox(boolean hasBox) {
        this.hasBox = hasBox;
        return this;
    }

    public boolean hasBox() {
        return hasBox;
    }


    public DataType getDataType() {
        return type;
    }
}